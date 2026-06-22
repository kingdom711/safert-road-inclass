package com.jinsung.safety_road_inclass.domain.feedback.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackPost;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackPostRepository extends JpaRepository<FeedbackPost, Long> {

    List<FeedbackPost> findByAuthorOrderByCreatedAtDesc(User author);

    List<FeedbackPost> findByStatusOrderByCreatedAtDesc(FeedbackStatus status);

    List<FeedbackPost> findAllByOrderByCreatedAtDesc();
}
