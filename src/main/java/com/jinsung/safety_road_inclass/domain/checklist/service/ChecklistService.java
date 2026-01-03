package com.jinsung.safety_road_inclass.domain.checklist.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.checklist.dto.*;
import com.jinsung.safety_road_inclass.domain.checklist.entity.*;
import com.jinsung.safety_road_inclass.domain.checklist.repository.ChecklistRepository;
import com.jinsung.safety_road_inclass.domain.template.entity.ChecklistTemplate;
import com.jinsung.safety_road_inclass.domain.template.entity.TemplateItem;
import com.jinsung.safety_road_inclass.domain.template.repository.ChecklistTemplateRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import com.jinsung.safety_road_inclass.global.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ChecklistService - 체크리스트 비즈니스 로직
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final ChecklistTemplateRepository templateRepository;
    private final StorageService storageService;

    /**
     * 체크리스트 제출
     */
    @Transactional
    public ChecklistResponse submit(ChecklistRequest request, 
                                    List<MultipartFile> files, 
                                    User currentUser) {
        // 1. 템플릿 조회 (존재 여부 및 활성화 확인)
        ChecklistTemplate template = templateRepository.findByIdWithItems(request.getTemplateId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

        // 2. 체크리스트 생성
        Checklist checklist = Checklist.builder()
                .template(template)
                .createdBy(currentUser)
                .siteName(request.getSiteName())
                .workDate(request.getWorkDate())
                .remarks(request.getRemarks())
                .build();

        // 3. 항목 추가 (위험 플래그 자동 설정)
        for (ChecklistItemRequest itemReq : request.getItems()) {
            TemplateItem templateItem = template.getItems().stream()
                    .filter(ti -> ti.getId().equals(itemReq.getTemplateItemId()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TEMPLATE_ITEM));

            ChecklistItem item = ChecklistItem.builder()
                    .templateItem(templateItem)
                    .answer(itemReq.getAnswer())
                    .comment(itemReq.getComment())
                    .build();

            checklist.addItem(item);
        }

        // 4. 사진 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String storedPath = storageService.store(file);
                    Photo photo = Photo.builder()
                            .originalName(file.getOriginalFilename())
                            .storedPath(storedPath)
                            .contentType(file.getContentType())
                            .fileSize(file.getSize())
                            .photoType(PhotoType.EVIDENCE)
                            .build();
                    checklist.addPhoto(photo);
                }
            }
        }

        // 5. 제출 처리
        checklist.submit();
        
        Checklist saved = checklistRepository.save(checklist);
        
        log.info("체크리스트 제출 완료: checklistId={}, userId={}, riskCount={}", 
                 saved.getId(), currentUser.getId(), saved.getRiskCount());

        return ChecklistResponse.from(saved);
    }

    /**
     * 내 체크리스트 목록 조회 (페이징)
     */
    public Page<ChecklistListResponse> getMyChecklists(User currentUser, Pageable pageable) {
        return checklistRepository
                .findByCreatedByIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(ChecklistListResponse::from);
    }

    /**
     * 체크리스트 상세 조회
     */
    public ChecklistResponse getChecklistDetail(Long checklistId, User currentUser) {
        Checklist checklist = checklistRepository.findByIdWithDetails(checklistId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHECKLIST_NOT_FOUND));

        // 권한 확인: 작성자 본인만 조회 가능 (또는 SUPERVISOR/ADMIN)
        if (!checklist.getCreatedBy().getId().equals(currentUser.getId()) 
                && !currentUser.getRole().name().equals("SUPERVISOR")
                && !currentUser.getRole().name().equals("ADMIN")) {
            throw new CustomException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        return ChecklistResponse.from(checklist);
    }

    /**
     * 상태별 체크리스트 조회 (검토 대기 목록 등)
     */
    public List<ChecklistListResponse> getChecklistsByStatus(ChecklistStatus status) {
        return checklistRepository.findByStatusOrderBySubmittedAtDesc(status).stream()
                .map(ChecklistListResponse::from)
                .toList();
    }

    /**
     * 위험 항목이 있는 체크리스트 조회
     */
    public List<ChecklistListResponse> getChecklistsWithRisk(ChecklistStatus status) {
        return checklistRepository.findWithRiskItems(status).stream()
                .map(ChecklistListResponse::from)
                .toList();
    }
}

