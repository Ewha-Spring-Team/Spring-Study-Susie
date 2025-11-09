package io.github.ewhaspringteam.spring_study_susie.dto;

import io.github.ewhaspringteam.spring_study_susie.entity.Comment;
import io.github.ewhaspringteam.spring_study_susie.entity.Post;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDto {

    private Long id;
    private String content;
    private String authorId;
    private String createdAt;
    private String updatedAt;
    private Long postId;
    private Long parentId;
    private Integer depth;
    private Boolean deleted;
    private List<CommentDto> children = new ArrayList<>();

    // 기본 생성자
    public CommentDto() {}

    // 댓글 작성용 생성자
    public CommentDto(String content, String authorId, Long postId, Long parentId) {
        this.content = content;
        this.authorId = authorId;
        this.postId = postId;
        this.parentId = parentId;
    }

    // Entity -> DTO 변환
    public static CommentDto fromEntity(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.id = comment.getId();
        dto.content = comment.getContent();
        dto.authorId = comment.getAuthorId();
        dto.createdAt = comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        if (comment.getUpdatedAt() != null) {
            dto.updatedAt = comment.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        dto.postId = comment.getPost().getId();
        dto.parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        dto.depth = comment.getDepth();
        dto.deleted = comment.getDeleted();
        
        // 자식 댓글들도 변환
        if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            dto.children = comment.getChildren().stream()
                    .map(CommentDto::fromEntity)
                    .collect(Collectors.toList());
        }
        
        return dto;
    }

    // Entity -> DTO 변환 (자식 댓글 제외 - 무한 재귀 방지)
    public static CommentDto fromEntityWithoutChildren(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.id = comment.getId();
        dto.content = comment.getContent();
        dto.authorId = comment.getAuthorId();
        dto.createdAt = comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        if (comment.getUpdatedAt() != null) {
            dto.updatedAt = comment.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        dto.postId = comment.getPost().getId();
        dto.parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        dto.depth = comment.getDepth();
        dto.deleted = comment.getDeleted();
        return dto;
    }

    // DTO -> Entity 변환 (새 댓글 생성용)
    public Comment toEntity(Post post, Comment parent) {
        if (parent == null) {
            // 최상위 댓글
            return new Comment(this.content, this.authorId, post);
        } else {
            // 대댓글
            return new Comment(this.content, this.authorId, post, parent);
        }
    }

    // 댓글 깊이에 따른 들여쓰기 문자열 생성
    public String getIndentation() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("　　"); // 전각 공백 2개씩
        }
        return sb.toString();
    }

    // 댓글 깊이에 따른 스타일 클래스 반환
    public String getDepthClass() {
        return "comment-depth-" + depth;
    }

    // 대댓글 여부 확인
    public boolean isReply() {
        return parentId != null && depth > 0;
    }

    // 답글 달기 가능 여부 확인 (최대 깊이 5, 삭제되지 않은 댓글)
    public boolean canReply() {
        return !deleted && depth < 5;
    }

    // 수정 가능 여부 확인 (작성자가 같고, 삭제되지 않은 댓글)
    public boolean isEditable(String currentUserId) {
        return !deleted && authorId.equals(currentUserId);
    }

    // 삭제 가능 여부 확인 (작성자가 같거나 관리자, 삭제되지 않은 댓글)
    public boolean isDeletable(String currentUserId) {
        return !deleted && (authorId.equals(currentUserId) || "admin".equals(currentUserId));
    }

    // 자식 댓글 추가
    public void addChild(CommentDto child) {
        this.children.add(child);
    }

    // 자식 댓글 개수
    public int getChildrenCount() {
        return children != null ? children.size() : 0;
    }

    // Getter와 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public List<CommentDto> getChildren() {
        return children;
    }

    public void setChildren(List<CommentDto> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "CommentDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", authorId='" + authorId + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", depth=" + depth +
                ", deleted=" + deleted +
                ", childrenCount=" + getChildrenCount() +
                '}';
    }
}