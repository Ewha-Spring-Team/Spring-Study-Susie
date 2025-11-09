package io.github.ewhaspringteam.spring_study_susie.dto;

import io.github.ewhaspringteam.spring_study_susie.entity.Post;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PostDto {

    private Long id;
    private String title;
    private String content;
    private String authorId;
    private String createdAt;
    private String resetTime;

    // 기본 생성자
    public PostDto() {}

    // 생성자 (게시글 작성용)
    public PostDto(String title, String content, String authorId, String resetTime) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.resetTime = resetTime;
    }

    // Entity -> DTO 변환
    public static PostDto fromEntity(Post post) {
        PostDto dto = new PostDto();
        dto.id = post.getId();
        dto.title = post.getTitle();
        dto.content = post.getContent();
        dto.authorId = post.getAuthorId();
        dto.createdAt = post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        dto.resetTime = post.getResetTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return dto;
    }

    // DTO -> Entity 변환
    public Post toEntity() {
        LocalDateTime resetDateTime;
        try {
            resetDateTime = LocalDateTime.parse(this.resetTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            // 기본값: 다음날 오전 9시
            resetDateTime = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        }
        return new Post(this.title, this.content, this.authorId, resetDateTime);
    }

    // Getter와 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getResetTime() {
        return resetTime;
    }

    public void setResetTime(String resetTime) {
        this.resetTime = resetTime;
    }

    @Override
    public String toString() {
        return "PostDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", authorId='" + authorId + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", resetTime='" + resetTime + '\'' +
                '}';
    }
}