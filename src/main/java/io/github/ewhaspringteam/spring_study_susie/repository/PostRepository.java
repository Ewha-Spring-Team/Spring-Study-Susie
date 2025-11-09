package io.github.ewhaspringteam.spring_study_susie.repository;

import io.github.ewhaspringteam.spring_study_susie.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 오늘 작성된 게시글이 있는지 확인 (하루에 단 하나의 게시글만 허용)
     */
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startOfDay AND p.createdAt < :endOfDay ORDER BY p.createdAt DESC")
    List<Post> findTodayPosts(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 오늘의 게시글 조회 (가장 최근 게시글)
     */
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startOfDay AND p.createdAt < :endOfDay ORDER BY p.createdAt DESC")
    Optional<Post> findTodayPost(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 현재 시간 이후로 초기화 시간이 설정된 게시글들 조회 (초기화 대상)
     */
    @Query("SELECT p FROM Post p WHERE p.resetTime <= :currentTime")
    List<Post> findPostsToReset(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 특정 작성자의 어제 게시글 확인 (이틀 연속 작성 방지)
     */
    @Query("SELECT p FROM Post p WHERE p.authorId = :authorId AND p.createdAt >= :startOfYesterday AND p.createdAt < :endOfYesterday")
    Optional<Post> findYesterdayPostByAuthor(@Param("authorId") String authorId, 
                                           @Param("startOfYesterday") LocalDateTime startOfYesterday, 
                                           @Param("endOfYesterday") LocalDateTime endOfYesterday);

    /**
     * 모든 게시글을 생성 시간 역순으로 조회
     */
    List<Post> findAllByOrderByCreatedAtDesc();
}