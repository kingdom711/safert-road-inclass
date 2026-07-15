package com.jinsung.safety_road_inclass.domain.feedback.repository;

import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackComment;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FeedbackCommentRepository extends JpaRepository<FeedbackComment, Long> {

    List<FeedbackComment> findByPostIdInOrderByCreatedAtAsc(Collection<Long> postIds);

    void deleteByPost(FeedbackPost post);
}
