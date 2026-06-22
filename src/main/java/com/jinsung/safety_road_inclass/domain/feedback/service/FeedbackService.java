package com.jinsung.safety_road_inclass.domain.feedback.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.feedback.dto.FeedbackCreateRequest;
import com.jinsung.safety_road_inclass.domain.feedback.dto.FeedbackPostResponse;
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

    public List<FeedbackPostResponse> getAll(FeedbackStatus status) {
        List<FeedbackPost> posts = status != null
                ? feedbackPostRepository.findByStatusOrderByCreatedAtDesc(status)
                : feedbackPostRepository.findAllByOrderByCreatedAtDesc();

        return posts.stream()
                .map(FeedbackPostResponse::from)
                .toList();
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
