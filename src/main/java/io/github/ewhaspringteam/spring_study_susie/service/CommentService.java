package io.github.ewhaspringteam.spring_study_susie.service;

import io.github.ewhaspringteam.spring_study_susie.dto.CommentDto;
import io.github.ewhaspringteam.spring_study_susie.entity.Comment;
import io.github.ewhaspringteam.spring_study_susie.entity.Post;
import io.github.ewhaspringteam.spring_study_susie.repository.CommentRepository;
import io.github.ewhaspringteam.spring_study_susie.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    /**
     * 게시글의 모든 댓글을 계층구조로 조회
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByPost(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }

        // 모든 댓글을 가져온 후 계층구조로 정리
        List<Comment> allComments = commentRepository.findActiveCommentsByPost(post.get());
        return buildCommentHierarchy(allComments);
    }

    /**
     * 댓글 목록을 계층구조로 구성
     */
    private List<CommentDto> buildCommentHierarchy(List<Comment> comments) {
        Map<Long, CommentDto> commentMap = new HashMap<>();
        List<CommentDto> rootComments = new ArrayList<>();

        // 모든 댓글을 DTO로 변환하여 Map에 저장
        for (Comment comment : comments) {
            CommentDto dto = CommentDto.fromEntityWithoutChildren(comment);
            commentMap.put(comment.getId(), dto);
        }

        // 계층구조 구성
        for (Comment comment : comments) {
            CommentDto dto = commentMap.get(comment.getId());
            
            if (comment.getParent() == null) {
                // 최상위 댓글
                rootComments.add(dto);
            } else {
                // 대댓글 - 부모에 추가
                CommentDto parentDto = commentMap.get(comment.getParent().getId());
                if (parentDto != null) {
                    parentDto.addChild(dto);
                }
            }
        }

        return rootComments;
    }

    /**
     * 새 댓글 작성
     */
    @Transactional
    public CommentDto createComment(CommentDto commentDto) {
        Optional<Post> post = postRepository.findById(commentDto.getPostId());
        if (post.isEmpty()) {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }

        Comment parent = null;
        if (commentDto.getParentId() != null) {
            Optional<Comment> parentComment = commentRepository.findById(commentDto.getParentId());
            if (parentComment.isEmpty()) {
                throw new RuntimeException("부모 댓글을 찾을 수 없습니다.");
            }
            parent = parentComment.get();
            
            // 댓글 깊이 제한 (최대 5단계)
            if (parent.getDepth() >= 4) {
                throw new RuntimeException("댓글은 최대 5단계까지만 작성할 수 있습니다.");
            }
        }

        Comment comment = commentDto.toEntity(post.get(), parent);
        Comment savedComment = commentRepository.save(comment);
        
        return CommentDto.fromEntityWithoutChildren(savedComment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentDto updateComment(Long commentId, String content, String authorId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            throw new RuntimeException("댓글을 찾을 수 없습니다.");
        }

        Comment comment = commentOpt.get();
        
        // 작성자 확인
        if (!comment.getAuthorId().equals(authorId)) {
            throw new RuntimeException("댓글 작성자만 수정할 수 있습니다.");
        }

        // 삭제된 댓글 확인
        if (comment.getDeleted()) {
            throw new RuntimeException("삭제된 댓글은 수정할 수 없습니다.");
        }

        comment.updateContent(content);
        Comment savedComment = commentRepository.save(comment);
        
        return CommentDto.fromEntityWithoutChildren(savedComment);
    }

    /**
     * 댓글 삭제 (논리적 삭제)
     */
    @Transactional
    public void deleteComment(Long commentId, String authorId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            throw new RuntimeException("댓글을 찾을 수 없습니다.");
        }

        Comment comment = commentOpt.get();
        
        // 작성자 또는 관리자 확인
        if (!comment.getAuthorId().equals(authorId) && !"admin".equals(authorId)) {
            throw new RuntimeException("댓글 작성자만 삭제할 수 있습니다.");
        }

        // 이미 삭제된 댓글 확인
        if (comment.getDeleted()) {
            throw new RuntimeException("이미 삭제된 댓글입니다.");
        }

        // 자식 댓글이 있는 경우 논리적 삭제, 없는 경우 물리적 삭제 가능
        List<Comment> children = commentRepository.findChildCommentsByParent(comment);
        
        if (children.isEmpty()) {
            // 자식 댓글이 없으면 물리적 삭제
            commentRepository.delete(comment);
        } else {
            // 자식 댓글이 있으면 논리적 삭제
            comment.markAsDeleted();
            commentRepository.save(comment);
        }
    }

    /**
     * 특정 댓글 조회
     */
    @Transactional(readOnly = true)
    public Optional<CommentDto> getCommentById(Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        return comment.map(CommentDto::fromEntityWithoutChildren);
    }

    /**
     * 특정 작성자의 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByAuthor(String authorId) {
        List<Comment> comments = commentRepository.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(authorId);
        return comments.stream()
                .map(CommentDto::fromEntityWithoutChildren)
                .collect(Collectors.toList());
    }

    /**
     * 게시글의 댓글 개수 조회
     */
    @Transactional(readOnly = true)
    public Long getCommentCountByPost(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            return 0L;
        }
        return commentRepository.countActiveCommentsByPost(post.get());
    }

    /**
     * 댓글 통계 조회 (총 댓글 수, 삭제된 댓글 수)
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getCommentStats(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            Map<String, Long> stats = new HashMap<>();
            stats.put("totalComments", 0L);
            stats.put("deletedComments", 0L);
            stats.put("activeComments", 0L);
            stats.put("maxDepth", 0L);
            return stats;
        }

        // 간단한 방법으로 댓글 통계 계산
        List<Comment> allComments = commentRepository.findAllCommentsByPost(post.get());
        
        long totalComments = allComments.size();
        long deletedComments = allComments.stream()
                .mapToLong(comment -> comment.getDeleted() ? 1 : 0)
                .sum();
        long activeComments = totalComments - deletedComments;
        
        int maxDepth = allComments.stream()
                .mapToInt(Comment::getDepth)
                .max()
                .orElse(0);

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalComments", totalComments);
        stats.put("deletedComments", deletedComments);
        stats.put("activeComments", activeComments);
        stats.put("maxDepth", (long) maxDepth);
        
        return stats;
    }

    /**
     * 최근 댓글 목록 조회 (관리용)
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getRecentComments(int limit) {
        List<Comment> comments = commentRepository.findAll()
                .stream()
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
        
        return comments.stream()
                .map(CommentDto::fromEntityWithoutChildren)
                .collect(Collectors.toList());
    }
}