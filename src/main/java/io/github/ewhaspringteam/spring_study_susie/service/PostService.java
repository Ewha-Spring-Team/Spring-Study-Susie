package io.github.ewhaspringteam.spring_study_susie.service;

import io.github.ewhaspringteam.spring_study_susie.dto.PostDto;
import io.github.ewhaspringteam.spring_study_susie.entity.Post;
import io.github.ewhaspringteam.spring_study_susie.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    /**
     * 오늘의 게시글 조회
     */
    public Optional<PostDto> getTodayPost() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        Optional<Post> post = postRepository.findTodayPost(startOfDay, endOfDay);
        return post.map(PostDto::fromEntity);
    }

    /**
     * 오늘 게시글이 이미 작성되었는지 확인
     */
    public boolean isTodayPostExists() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<Post> todayPosts = postRepository.findTodayPosts(startOfDay, endOfDay);
        return !todayPosts.isEmpty();
    }

    /**
     * 특정 사용자가 어제 게시글을 작성했는지 확인 (이틀 연속 작성 방지)
     */
    public boolean didUserPostYesterday(String authorId) {
        LocalDateTime startOfYesterday = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfYesterday = startOfYesterday.plusDays(1);
        Optional<Post> yesterdayPost = postRepository.findYesterdayPostByAuthor(authorId, startOfYesterday, endOfYesterday);
        return yesterdayPost.isPresent();
    }

    /**
     * 게시글 작성 가능 여부 확인
     */
    public String checkCanCreatePost(String authorId) {
        // 1. 오늘 이미 게시글이 있는지 확인
        if (isTodayPostExists()) {
            return "오늘은 이미 게시글이 작성되었습니다. 내일 다시 시도해주세요!";
        }

        // 2. 어제 같은 사용자가 작성했는지 확인
        if (didUserPostYesterday(authorId)) {
            return "어제 게시글을 작성하셨네요! 이틀 연속 작성은 불가능합니다.";
        }

        return "OK"; // 작성 가능
    }

    /**
     * 게시글 작성
     */
    public PostDto createPost(PostDto postDto) {
        // 작성 가능 여부 재확인
        String canCreate = checkCanCreatePost(postDto.getAuthorId());
        if (!"OK".equals(canCreate)) {
            throw new RuntimeException(canCreate);
        }

        Post post = postDto.toEntity();
        Post savedPost = postRepository.save(post);
        return PostDto.fromEntity(savedPost);
    }

    /**
     * 게시글 상세 조회
     */
    public Optional<PostDto> getPostById(Long id) {
        Optional<Post> post = postRepository.findById(id);
        return post.map(PostDto::fromEntity);
    }

    /**
     * 모든 게시글 조회 (최신순)
     */
    public List<PostDto> getAllPosts() {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        return posts.stream()
                .map(PostDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 초기화 시간이 지난 게시글들 삭제 (스케줄러에서 호출)
     */
    public void resetExpiredPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> expiredPosts = postRepository.findPostsToReset(now);
        
        if (!expiredPosts.isEmpty()) {
            postRepository.deleteAll(expiredPosts);
            System.out.println("초기화된 게시글 수: " + expiredPosts.size());
        }
    }
}