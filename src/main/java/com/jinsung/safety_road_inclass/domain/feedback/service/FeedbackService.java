package com.jinsung.safety_road_inclass.domain.feedback.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.feedback.dto.FeedbackCreateRequest;
import com.jinsung.safety_road_inclass.domain.feedback.dto.FeedbackPostResponse;
import com.jinsung.safety_road_inclass.domain.feedback.dto.NoticeCreateRequest;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackCategory;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackPost;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackStatus;
import com.jinsung.safety_road_inclass.domain.feedback.repository.FeedbackPostRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackPostRepository feedbackPostRepository;

    @Transactional
    public FeedbackPostResponse create(FeedbackCreateRequest request, User author) {
        FeedbackPost post = FeedbackPost.builder()
                .author(author)
                .category(request.getCategory())
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .pagePath(trimToNull(request.getPagePath()))
                .attachmentUrl(trimToNull(request.getAttachmentUrl()))
                .build();

        return FeedbackPostResponse.from(feedbackPostRepository.save(post));
    }

    public List<FeedbackPostResponse> getMine(User author) {
        return feedbackPostRepository.findByAuthorOrderByCreatedAtDesc(author).stream()
                .map(FeedbackPostResponse::from)
                .toList();
    }

    // 공지(NOTICE)는 별도 목록으로 관리하므로 관리자 의견 목록에서 제외
    public List<FeedbackPostResponse> getAll(FeedbackStatus status) {
        List<FeedbackPost> posts = status != null
                ? feedbackPostRepository.findByStatusAndCategoryNotOrderByCreatedAtDesc(status, FeedbackCategory.NOTICE)
                : feedbackPostRepository.findByCategoryNotOrderByCreatedAtDesc(FeedbackCategory.NOTICE);

        return posts.stream()
                .map(FeedbackPostResponse::from)
                .toList();
    }

    public List<FeedbackPostResponse> getNotices() {
        return feedbackPostRepository.findByCategoryOrderByCreatedAtDesc(FeedbackCategory.NOTICE).stream()
                .map(FeedbackPostResponse::from)
                .toList();
    }

    @Transactional
    public FeedbackPostResponse createNotice(NoticeCreateRequest request, User admin) {
        FeedbackPost post = FeedbackPost.builder()
                .author(admin)
                .category(FeedbackCategory.NOTICE)
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .build();
        // 공지는 답변/처리 흐름이 없으므로 대기 건수에 잡히지 않도록 종료 상태로 저장
        post.updateStatus(FeedbackStatus.CLOSED);

        return FeedbackPostResponse.from(feedbackPostRepository.save(post));
    }

    @Transactional
    public void deleteNotice(Long postId) {
        FeedbackPost post = findPost(postId);
        if (post.getCategory() != FeedbackCategory.NOTICE) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        feedbackPostRepository.delete(post);
    }

    @Transactional
    public FeedbackPostResponse updateStatus(Long postId, FeedbackStatus status) {
        FeedbackPost post = findPost(postId);
        post.updateStatus(status);
        return FeedbackPostResponse.from(post);
    }

    @Transactional
    public FeedbackPostResponse reply(Long postId, String reply, User admin) {
        FeedbackPost post = findPost(postId);
        post.reply(reply.trim(), admin);
        return FeedbackPostResponse.from(post);
    }

    private FeedbackPost findPost(Long postId) {
        return feedbackPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
