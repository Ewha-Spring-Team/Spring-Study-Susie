package io.github.ewhaspringteam.spring_study_susie.controller;

import io.github.ewhaspringteam.spring_study_susie.dto.PostDto;
import io.github.ewhaspringteam.spring_study_susie.service.PostService;
import io.github.ewhaspringteam.spring_study_susie.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    /**
     * 메인 페이지 - 오늘의 게시글 또는 게시글 작성 페이지
     */
    @GetMapping("/")
    public String index(Model model) {
        Optional<PostDto> todayPost = postService.getTodayPost();
        
        if (todayPost.isPresent()) {
            // 오늘의 게시글이 있으면 표시
            PostDto post = todayPost.get();
            model.addAttribute("post", post);
            
            // 댓글 정보 추가
            var comments = commentService.getCommentsByPost(post.getId());
            var commentStats = commentService.getCommentStats(post.getId());
            
            model.addAttribute("comments", comments);
            model.addAttribute("commentStats", commentStats);
            model.addAttribute("newComment", new io.github.ewhaspringteam.spring_study_susie.dto.CommentDto());
            
            return "post/today";
        } else {
            // 오늘의 게시글이 없으면 작성 페이지로
            return "redirect:/posts/new";
        }
    }

    /**
     * 게시글 작성 페이지
     */
    @GetMapping("/posts/new")
    public String newPost(Model model) {
        model.addAttribute("postDto", new PostDto());
        return "post/new";
    }

    /**
     * 게시글 작성 처리
     */
    @PostMapping("/posts")
    public String createPost(PostDto postDto, RedirectAttributes redirectAttributes) {
        try {
            PostDto savedPost = postService.createPost(postDto);
            redirectAttributes.addFlashAttribute("message", "오늘의 게시글이 성공적으로 작성되었습니다!");
            return "redirect:/";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/posts/new";
        }
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/posts/{id}")
    public String showPost(@PathVariable Long id, Model model) {
        Optional<PostDto> post = postService.getPostById(id);
        
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            
            // 댓글 정보 추가
            var comments = commentService.getCommentsByPost(id);
            var commentStats = commentService.getCommentStats(id);
            
            model.addAttribute("comments", comments);
            model.addAttribute("commentStats", commentStats);
            model.addAttribute("newComment", new io.github.ewhaspringteam.spring_study_susie.dto.CommentDto());
            
            return "post/show";
        } else {
            return "redirect:/";
        }
    }

    /**
     * 모든 게시글 목록 (히스토리)
     */
    @GetMapping("/posts")
    public String listPosts(Model model) {
        List<PostDto> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return "post/list";
    }

    /**
     * 게시글 작성 가능 여부 확인 API
     */
    @GetMapping("/posts/check")
    public String checkCanCreate(String authorId, Model model) {
        String result = postService.checkCanCreatePost(authorId);
        model.addAttribute("canCreate", "OK".equals(result));
        model.addAttribute("message", result);
        model.addAttribute("authorId", authorId);
        return "post/check";
    }
}