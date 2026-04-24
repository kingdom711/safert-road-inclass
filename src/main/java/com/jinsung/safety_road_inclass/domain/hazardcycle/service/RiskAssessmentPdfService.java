package com.jinsung.safety_road_inclass.domain.hazardcycle.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReport;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReportAck;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportAckRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.EnumMap;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 위험성평가표 PDF 생성 서비스.
 *
 * 고용노동부 고시 제2023-19호「사업장 위험성평가에 관한 지침」별지 양식을
 * 참고하여, Hazard Cycle 1건을 점검관 제출 가능한 양식으로 렌더링한다.
 *
 * 한글 폰트 요구: classpath:/fonts/NotoSansKR-Regular.ttf 필요.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentPdfService {

    private static final String KOREAN_FONT_PATH = "fonts/NotoSansKR-Regular.ttf";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObjectMapper objectMapper;
    private final HazardReportAckRepository hazardReportAckRepository;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public byte[] generate(HazardReport report) {
        List<HazardReportAck> acks = hazardReportAckRepository.findByHazardReportIdOrderByAckedAtAsc(report.getId());
        return generate(report, acks);
    }

    public byte[] generate(HazardReport report, List<HazardReportAck> acks) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDFont font = loadKoreanFont(doc);
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDImageXObject qrImage = buildQrImage(doc, report);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                Renderer r = new Renderer(cs, font, page);
                r.drawDocument(report, acks, qrImage);
            }

            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("위험성평가표 PDF 생성 실패: reportId={}", report.getId(), e);
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "PDF 생성에 실패했습니다: " + e.getMessage());
        }
    }

    private PDImageXObject buildQrImage(PDDocument doc, HazardReport report) {
        if (report.getCertNumber() == null || report.getCertNumber().isBlank()) {
            return null;
        }
        String url = publicBaseUrl.replaceAll("/+$", "") + "/api/v1/hazard-verify/" + report.getCertNumber();
        try {
            EnumMap<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 200, 200, hints);
            BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);
            return LosslessFactory.createFromImage(doc, img);
        } catch (WriterException | IOException e) {
            log.warn("QR 코드 생성 실패: certNumber={}", report.getCertNumber(), e);
            return null;
        }
    }

    private PDFont loadKoreanFont(PDDocument doc) throws IOException {
        ClassPathResource resource = new ClassPathResource(KOREAN_FONT_PATH);
        if (!resource.exists()) {
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED,
                    "한글 폰트 파일이 필요합니다. resources/fonts/NotoSansKR-Regular.ttf 를 추가해주세요.");
        }
        try (InputStream is = resource.getInputStream()) {
            return PDType0Font.load(doc, is, true);
        }
    }

    /**
     * PDF 내용 렌더링. 단일 페이지 기준으로 상단부터 커서를 내리며 그린다.
     */
    private class Renderer {
        private final PDPageContentStream cs;
        private final PDFont font;
        private final float pageWidth;
        private final float pageHeight;
        private final float margin = 40f;
        private float cursorY;

        Renderer(PDPageContentStream cs, PDFont font, PDPage page) {
            this.cs = cs;
            this.font = font;
            this.pageWidth = page.getMediaBox().getWidth();
            this.pageHeight = page.getMediaBox().getHeight();
            this.cursorY = pageHeight - margin;
        }

        void drawDocument(HazardReport r, List<HazardReportAck> acks, PDImageXObject qrImage) throws IOException {
            drawHeader("위험성평가표");
            drawSubHeader("고용노동부 고시 제2023-19호 「사업장 위험성평가에 관한 지침」 준수");
            gap(12);
            drawHorizontalLine();
            gap(10);

            drawLabelValue("사이클 ID", "#" + r.getId());
            drawLabelValue("보고자", r.getReporter() != null ? safe(r.getReporter().getName()) : "-");
            drawLabelValue("보고 일시", r.getReportedAt() != null ? r.getReportedAt().format(DATE_FMT) : "-");
            drawLabelValue("위치", safe(r.getLocationDescription()));
            drawLabelValue("현장 설명", safe(r.getHazardDescription()));
            gap(8);

            drawSectionTitle("1. 유해·위험요인 식별");
            drawLabelValue("분류(KRAS)", safe(r.getAiHazardClassification()));
            drawLabelValue("불안전 상태", safe(r.getAiUnsafeCondition()));
            drawLabelValue("불안전 행동", safe(r.getAiUnsafeAct()));
            drawLabelValue("예상 재해", safe(r.getAiPossibleAccident()));
            gap(8);

            drawSectionTitle("2. 위험성 추정 (빈도 × 강도 = 위험성)");
            drawLabelValue("빈도 L", scoreText(r.getAiLikelihoodScore(), r.getAiLikelihoodRationale()));
            drawLabelValue("강도 S", scoreText(r.getAiSeverityScore(), r.getAiSeverityRationale()));
            drawLabelValue("위험성 R", (r.getAiRiskScore() != null ? r.getAiRiskScore() + " (" : "- (")
                    + safe(r.getAiRiskLevel()) + ")");
            gap(8);

            drawSectionTitle("3. 위험요인 요약");
            drawMultiline(safe(r.getAiRiskFactor()));
            gap(8);

            drawSectionTitle("4. 법적 근거");
            List<Map<String, String>> legal = parseLegalBasis(r.getAiLegalBasisJson());
            if (legal.isEmpty()) {
                drawMultiline("- (없음)");
            } else {
                for (Map<String, String> item : legal) {
                    String line = "· " + safe(item.get("law")) + " " + safe(item.get("article"));
                    String content = item.get("content");
                    if (content != null && !content.isBlank()) {
                        line += " — " + content;
                    }
                    drawMultiline(line);
                }
            }
            gap(8);

            drawSectionTitle("5. 통제 위계 (Hierarchy of Controls)");
            Map<String, List<String>> controls = parseControlMeasures(r.getAiControlMeasuresJson());
            drawControlGroup("즉시 조치", controls.get("immediate"));
            drawControlGroup("공학적 대책", controls.get("engineering"));
            drawControlGroup("관리적 대책", controls.get("administrative"));
            drawControlGroup("개인보호구", controls.get("ppe"));
            gap(8);

            drawSectionTitle("6. 조치 책임 및 기한");
            drawLabelValue("책임자", safe(r.getAiResponsibleRole()));
            drawLabelValue("조치 기한", r.getAiDueDays() != null ? "D+" + r.getAiDueDays() : "-");
            drawLabelValue("KOSHA Guide", safe(r.getAiKoshaGuide() != null ? r.getAiKoshaGuide() : r.getAiReferenceCode()));
            drawLabelValue("AI 신뢰도", r.getAiConfidence() != null
                    ? String.format("%.0f%%", r.getAiConfidence() * 100) : "-");
            gap(8);

            drawSectionTitle("7. 근로자 참여 확인 (산안법 제36조 2항)");
            if (acks == null || acks.isEmpty()) {
                drawMultiline("- 동료 근로자 확인 없음");
            } else {
                drawLabelValue("참여 인원", acks.size() + "명");
                for (HazardReportAck ack : acks) {
                    String name = ack.getAcker() != null ? ack.getAcker().getName() : "-";
                    String when = ack.getAckedAt() != null ? ack.getAckedAt().format(DATE_FMT) : "-";
                    String line = "· " + name + " (" + when + ")";
                    if (ack.getComment() != null && !ack.getComment().isBlank()) {
                        line += " — " + ack.getComment();
                    }
                    drawMultiline(line);
                }
            }
            gap(8);

            drawSectionTitle("8. 조치 완료 기록");
            drawLabelValue("완료 일시", r.getCompletedAt() != null ? r.getCompletedAt().format(DATE_FMT) : "미완료");
            drawLabelValue("조치 메모", safe(r.getCompletionNote()));

            gap(14);
            drawHorizontalLine();
            gap(6);
            drawIntegritySection(r, qrImage);
            drawSmall("※ 본 문서는 산업안전보건법 제36조 위험성평가 이행 증빙용으로 자동 생성되었습니다.");
            drawSmall("   AI 분석 일시: " + (r.getAiAnalyzedAt() != null ? r.getAiAnalyzedAt().format(DATE_FMT) : "-"));
        }

        private void drawIntegritySection(HazardReport r, PDImageXObject qrImage) throws IOException {
            float qrSize = 90f;
            float topY = cursorY;
            float textX = margin;
            float qrX = pageWidth - margin - qrSize;

            if (qrImage != null) {
                cs.drawImage(qrImage, qrX, topY - qrSize, qrSize, qrSize);
            }

            // 좌측 텍스트 블록 (QR과 같은 상단에서 시작)
            float lineH = 12f;
            float y = topY;
            drawText("[원본 검증] QR 스캔 또는 아래 URL 접속", 9f, textX, y, new Color(40, 40, 40));
            y -= lineH;
            drawText("인증번호: " + safe(r.getCertNumber()), 9f, textX, y, Color.BLACK);
            y -= lineH;
            drawText("해시: " + shortHash(r.getIntegrityHash()), 9f, textX, y, Color.BLACK);
            y -= lineH;
            drawText("이전 해시: " + shortHash(r.getPrevHash()), 9f, textX, y, new Color(90, 90, 90));
            y -= lineH;
            drawText("사진 SHA-256: " + shortHash(r.getPhotoSha256()), 9f, textX, y, new Color(90, 90, 90));
            y -= lineH;
            String verifyUrl = publicBaseUrl.replaceAll("/+$", "") + "/api/v1/hazard-verify/"
                    + safe(r.getCertNumber());
            drawText(verifyUrl, 8f, textX, y, new Color(30, 80, 160));

            cursorY = Math.min(y, topY - qrSize) - 8;
        }

        private String shortHash(String s) {
            if (s == null || s.isBlank()) return "-";
            if (s.length() <= 20) return s;
            return s.substring(0, 12) + "..." + s.substring(s.length() - 8);
        }

        private void drawHeader(String text) throws IOException {
            drawText(text, 20f, margin, cursorY, Color.BLACK);
            cursorY -= 24;
        }

        private void drawSubHeader(String text) throws IOException {
            drawText(text, 10f, margin, cursorY, new Color(90, 90, 90));
            cursorY -= 14;
        }

        private void drawSectionTitle(String text) throws IOException {
            cs.setNonStrokingColor(new Color(230, 240, 255));
            cs.addRect(margin, cursorY - 4, pageWidth - margin * 2, 16);
            cs.fill();
            drawText(text, 11f, margin + 4, cursorY + 2, new Color(20, 60, 120));
            cursorY -= 20;
        }

        private void drawLabelValue(String label, String value) throws IOException {
            float labelW = 100f;
            drawText(label, 9f, margin, cursorY, new Color(100, 100, 100));
            drawText(value != null && !value.isBlank() ? value : "-", 10f, margin + labelW, cursorY, Color.BLACK);
            cursorY -= 14;
        }

        private void drawMultiline(String text) throws IOException {
            if (text == null) text = "";
            for (String line : wrap(text, 90)) {
                drawText(line, 10f, margin + 8, cursorY, Color.BLACK);
                cursorY -= 13;
            }
        }

        private void drawControlGroup(String label, List<String> items) throws IOException {
            if (items == null || items.isEmpty()) return;
            drawText("▸ " + label, 10f, margin + 4, cursorY, new Color(30, 80, 160));
            cursorY -= 13;
            for (String item : items) {
                for (String line : wrap("  · " + item, 88)) {
                    drawText(line, 9.5f, margin + 12, cursorY, Color.BLACK);
                    cursorY -= 12;
                }
            }
            cursorY -= 2;
        }

        private void drawSmall(String text) throws IOException {
            drawText(text, 8f, margin, cursorY, new Color(120, 120, 120));
            cursorY -= 11;
        }

        private void drawHorizontalLine() throws IOException {
            cs.setStrokingColor(new Color(200, 200, 200));
            cs.moveTo(margin, cursorY);
            cs.lineTo(pageWidth - margin, cursorY);
            cs.stroke();
        }

        private void drawText(String text, float size, float x, float y, Color color) throws IOException {
            if (text == null) text = "";
            cs.beginText();
            cs.setFont(font, size);
            cs.setNonStrokingColor(color);
            cs.newLineAtOffset(x, y);
            cs.showText(text);
            cs.endText();
        }

        private void gap(float h) {
            cursorY -= h;
        }

        private List<String> wrap(String text, int maxChars) {
            if (text.length() <= maxChars) {
                return List.of(text);
            }
            java.util.ArrayList<String> lines = new java.util.ArrayList<>();
            int i = 0;
            while (i < text.length()) {
                int end = Math.min(i + maxChars, text.length());
                lines.add(text.substring(i, end));
                i = end;
            }
            return lines;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String scoreText(Integer score, String rationale) {
        if (score == null && (rationale == null || rationale.isBlank())) return "-";
        StringBuilder sb = new StringBuilder();
        if (score != null) sb.append(score);
        if (rationale != null && !rationale.isBlank()) {
            if (sb.length() > 0) sb.append(" — ");
            sb.append(rationale);
        }
        return sb.toString();
    }

    private List<Map<String, String>> parseLegalBasis(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {
            });
        } catch (IOException e) {
            log.warn("법적 근거 JSON 파싱 실패: {}", json, e);
            return List.of();
        }
    }

    private Map<String, List<String>> parseControlMeasures(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, List<String>>>() {
            });
        } catch (IOException e) {
            log.warn("통제 수단 JSON 파싱 실패: {}", json, e);
            return Map.of();
        }
    }
}
