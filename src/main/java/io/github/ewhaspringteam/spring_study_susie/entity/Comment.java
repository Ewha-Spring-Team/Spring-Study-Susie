package io.github.ewhaspringteam.spring_study_susie.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String authorId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // 게시글과의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 부모 댓글과의 관계 (대댓글을 위한 자기참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 자식 댓글들 (대댓글들)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    // 댓글 깊이 (depth) - 0: 최상위 댓글, 1: 1차 대댓글, 2: 2차 대댓글...
    @Column(nullable = false)
    private Integer depth = 0;

    // 삭제 여부 (실제 삭제가 아닌 논리적 삭제)
    @Column(nullable = false)
    private Boolean deleted = false;

    // 기본 생성자
    public Comment() {}

    // 생성자 (최상위 댓글용)
    public Comment(String content, String authorId, Post post) {
        this.content = content;
        this.authorId = authorId;
        this.post = post;
        this.createdAt = LocalDateTime.now();
        this.depth = 0;
        this.deleted = false;
    }

    // 생성자 (대댓글용)
    public Comment(String content, String authorId, Post post, Comment parent) {
        this.content = content;
        this.authorId = authorId;
        this.post = post;
        this.parent = parent;
        this.createdAt = LocalDateTime.now();
        this.depth = parent.getDepth() + 1;
        this.deleted = false;
    }

    // 댓글 수정
    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    // 댓글 삭제 (논리적 삭제)
    public void markAsDeleted() {
        this.deleted = true;
        this.content = "삭제된 댓글입니다.";
        this.updatedAt = LocalDateTime.now();
    }

    // 자식 댓글 추가
    public void addChild(Comment child) {
        children.add(child);
        child.setParent(this);
    }

    // 자식 댓글 제거
    public void removeChild(Comment child) {
        children.remove(child);
        child.setParent(null);
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Comment getParent() {
        return parent;
    }

    public void setParent(Comment parent) {
        this.parent = parent;
    }

    public List<Comment> getChildren() {
        return children;
    }

    public void setChildren(List<Comment> children) {
        this.children = children;
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

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", authorId='" + authorId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", depth=" + depth +
                ", deleted=" + deleted +
                '}';
    }
}