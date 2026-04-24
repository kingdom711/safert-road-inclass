package com.jinsung.safety_road_inclass.domain.hazardcycle.service;

import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.CycleStatus;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReport;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 월간 위험요인 개선대장 Excel 생성.
 * 고용노동부 점검관에게 제출하는 사업장 단위 개선대장 양식.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HazardLogExcelService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String[] HEADERS = {
            "No", "사이클ID", "보고일시", "보고자", "위치",
            "위험분류(KRAS)", "불안전 상태", "불안전 행동",
            "예상재해", "빈도(L)", "강도(S)", "위험성(R)", "위험등급",
            "법적근거", "KOSHA Guide", "책임자", "조치기한",
            "조치완료일시", "조치메모", "상태"
    };

    private final HazardReportRepository hazardReportRepository;

    public byte[] generateMonthly(int year, int month) {
        YearMonth ym;
        try {
            ym = YearMonth.of(year, month);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 연/월입니다: " + year + "-" + month);
        }

        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<HazardReport> reports = hazardReportRepository.findByReportedAtBetweenOrderByReportedAtAsc(start, end);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(String.format("%d-%02d", year, month));

            CellStyle titleStyle = titleStyle(wb);
            CellStyle headerStyle = headerStyle(wb);
            CellStyle cellStyle = cellStyle(wb);
            CellStyle highRiskStyle = highRiskStyle(wb);

            int rowIdx = 0;

            // 제목
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(String.format("월간 위험요인 개선대장 (%d년 %d월)", year, month));
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, HEADERS.length - 1));
            rowIdx++; // 한 줄 띄움

            // 헤더
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(HEADERS[i]);
                c.setCellStyle(headerStyle);
            }

            // 데이터
            int no = 1;
            for (HazardReport r : reports) {
                Row row = sheet.createRow(rowIdx++);
                boolean highRisk = "CRITICAL".equals(r.getAiRiskLevel()) || "HIGH".equals(r.getAiRiskLevel());
                CellStyle rowStyle = highRisk ? highRiskStyle : cellStyle;

                int col = 0;
                setCell(row, col++, no++, rowStyle);
                setCell(row, col++, r.getId() != null ? r.getId().toString() : "", rowStyle);
                setCell(row, col++, r.getReportedAt() != null ? r.getReportedAt().format(DATE_FMT) : "", rowStyle);
                setCell(row, col++, r.getReporter() != null ? nullSafe(r.getReporter().getName()) : "", rowStyle);
                setCell(row, col++, nullSafe(r.getLocationDescription()), rowStyle);
                setCell(row, col++, nullSafe(r.getAiHazardClassification()), rowStyle);
                setCell(row, col++, nullSafe(r.getAiUnsafeCondition()), rowStyle);
                setCell(row, col++, nullSafe(r.getAiUnsafeAct()), rowStyle);
                setCell(row, col++, nullSafe(r.getAiPossibleAccident()), rowStyle);
                setCell(row, col++, r.getAiLikelihoodScore(), rowStyle);
                setCell(row, col++, r.getAiSeverityScore(), rowStyle);
                setCell(row, col++, r.getAiRiskScore(), rowStyle);
                setCell(row, col++, nullSafe(r.getAiRiskLevel()), rowStyle);
                setCell(row, col++, summarizeLegal(r.getAiLegalBasisJson()), rowStyle);
                setCell(row, col++, nullSafe(r.getAiKoshaGuide() != null ? r.getAiKoshaGuide() : r.getAiReferenceCode()), rowStyle);
                setCell(row, col++, nullSafe(r.getAiResponsibleRole()), rowStyle);
                setCell(row, col++, r.getAiDueDays() != null ? "D+" + r.getAiDueDays() : "", rowStyle);
                setCell(row, col++, r.getCompletedAt() != null ? r.getCompletedAt().format(DATE_FMT) : "", rowStyle);
                setCell(row, col++, nullSafe(r.getCompletionNote()), rowStyle);
                setCell(row, col++, statusLabel(r.getStatus()), rowStyle);
            }

            // 요약
            rowIdx++;
            long total = reports.size();
            long completed = reports.stream().filter(h -> h.getStatus() == CycleStatus.ACTION_COMPLETED).count();
            long critical = reports.stream().filter(h -> "CRITICAL".equals(h.getAiRiskLevel())).count();
            long high = reports.stream().filter(h -> "HIGH".equals(h.getAiRiskLevel())).count();

            Row sumRow = sheet.createRow(rowIdx++);
            sumRow.createCell(0).setCellValue(
                    String.format("총 %d건  /  조치완료 %d건(%s%%)  /  CRITICAL %d건  /  HIGH %d건",
                            total, completed,
                            total == 0 ? "0" : String.format("%.1f", completed * 100.0 / total),
                            critical, high));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                    rowIdx - 1, rowIdx - 1, 0, HEADERS.length - 1));

            Row legalRow = sheet.createRow(rowIdx);
            legalRow.createCell(0).setCellValue(
                    "※ 본 대장은 산업안전보건법 제36조 및 고용노동부 고시 제2023-19호 위험성평가 이행 증빙용입니다.");
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx, rowIdx, 0, HEADERS.length - 1));

            // 컬럼 폭 자동 조정 (최대 40자)
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                if (width > 40 * 256) {
                    sheet.setColumnWidth(i, 40 * 256);
                }
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("월간 위험요인 개선대장 Excel 생성 실패", e);
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "Excel 생성에 실패했습니다: " + e.getMessage());
        }
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        c.setCellStyle(style);
    }

    private void setCell(Row row, int col, int value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void setCell(Row row, int col, Integer value, CellStyle style) {
        Cell c = row.createCell(col);
        if (value != null) c.setCellValue(value);
        c.setCellStyle(style);
    }

    private CellStyle titleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private CellStyle cellStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setWrapText(true);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle highRiskStyle(Workbook wb) {
        CellStyle s = cellStyle(wb);
        s.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private String summarizeLegal(String legalBasisJson) {
        if (legalBasisJson == null || legalBasisJson.isBlank()) return "";
        // JSON을 완전히 파싱하지 않고 "article" 값만 간이 추출 (과도한 파싱 비용 방지)
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"article\"\\s*:\\s*\"([^\"]+)\"").matcher(legalBasisJson);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(m.group(1));
        }
        return sb.toString();
    }

    private String statusLabel(CycleStatus status) {
        if (status == null) return "";
        return switch (status) {
            case HAZARD_REPORTED -> "신고됨";
            case AI_ANALYZED -> "분석완료";
            case ACTION_COMPLETED -> "조치완료";
            case EXPIRED -> "만료";
            case REJECTED -> "반려";
        };
    }
}
