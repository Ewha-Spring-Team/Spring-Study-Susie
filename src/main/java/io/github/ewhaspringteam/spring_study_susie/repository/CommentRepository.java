package io.github.ewhaspringteam.spring_study_susie.repository;

import io.github.ewhaspringteam.spring_study_susie.entity.Comment;
import io.github.ewhaspringteam.spring_study_susie.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글의 모든 댓글을 계층구조로 조회
     * 최상위 댓글(parent가 null)을 먼저 조회하고, 각각의 자식들은 엔티티 관계로 fetch
     */
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.parent IS NULL AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByPost(@Param("post") Post post);

    /**
     * 특정 게시글의 모든 댓글 조회 (삭제된 댓글 포함, depth 순서로)
     */
    @Query("SELECT c FROM Comment c WHERE c.post = :post ORDER BY c.createdAt ASC")
    List<Comment> findAllCommentsByPost(@Param("post") Post post);

    /**
     * 특정 게시글의 삭제되지 않은 댓글만 조회 (depth 순서로)
     */
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findActiveCommentsByPost(@Param("post") Post post);

    /**
     * 특정 부모 댓글의 자식 댓글들 조회 (대댓글)
     */
    @Query("SELECT c FROM Comment c WHERE c.parent = :parent AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findChildCommentsByParent(@Param("parent") Comment parent);

    /**
     * 특정 작성자의 댓글들 조회
     */
    List<Comment> findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(String authorId);

    /**
     * 특정 게시글의 댓글 개수 조회 (삭제된 댓글 제외)
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post AND c.deleted = false")
    Long countActiveCommentsByPost(@Param("post") Post post);

    /**
     * 특정 부모 댓글의 자식 댓글 개수 조회
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parent = :parent AND c.deleted = false")
    Long countChildCommentsByParent(@Param("parent") Comment parent);

    /**
     * 특정 깊이의 댓글들 조회
     */
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.depth = :depth AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findCommentsByPostAndDepth(@Param("post") Post post, @Param("depth") Integer depth);

    /**
     * 특정 댓글의 모든 하위 댓글들을 재귀적으로 조회 (삭제 시 사용)
     */
    @Query("SELECT c FROM Comment c WHERE c.parent = :parent")
    List<Comment> findAllChildCommentsByParent(@Param("parent") Comment parent);

    /**
     * 게시글별 댓글 통계 (총 댓글 수, 삭제된 댓글 수)
     */
    @Query("SELECT COUNT(c), SUM(CASE WHEN c.deleted = true THEN 1 ELSE 0 END) FROM Comment c WHERE c.post = :post")
    Object[] getCommentStatsByPost(@Param("post") Post post);
}