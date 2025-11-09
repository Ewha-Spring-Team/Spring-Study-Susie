package io.github.ewhaspringteam.spring_study_susie.controller;

import io.github.ewhaspringteam.spring_study_susie.dto.CommentDto;
import io.github.ewhaspringteam.spring_study_susie.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 댓글 작성
     */
    @PostMapping("/posts/{postId}/comments")
    public String createComment(@PathVariable Long postId,
                              @ModelAttribute CommentDto commentDto,
                              RedirectAttributes redirectAttributes) {
        try {
            commentDto.setPostId(postId);
            CommentDto savedComment = commentService.createComment(commentDto);
            redirectAttributes.addFlashAttribute("message", "댓글이 성공적으로 작성되었습니다!");
            return "redirect:/posts/" + postId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/posts/" + postId;
        }
    }

    /**
     * 대댓글 작성
     */
    @PostMapping("/posts/{postId}/comments/{parentId}/reply")
    public String createReply(@PathVariable Long postId,
                            @PathVariable Long parentId,
                            @ModelAttribute CommentDto commentDto,
                            RedirectAttributes redirectAttributes) {
        try {
            commentDto.setPostId(postId);
            commentDto.setParentId(parentId);
            CommentDto savedComment = commentService.createComment(commentDto);
            redirectAttributes.addFlashAttribute("message", "답글이 성공적으로 작성되었습니다!");
            return "redirect:/posts/" + postId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/posts/" + postId;
        }
    }

    /**
     * 댓글 수정
     */
    @PostMapping("/comments/{commentId}/update")
    public String updateComment(@PathVariable Long commentId,
                              @RequestParam String content,
                              @RequestParam String authorId,
                              @RequestParam Long postId,
                              RedirectAttributes redirectAttributes) {
        try {
            CommentDto updatedComment = commentService.updateComment(commentId, content, authorId);
            redirectAttributes.addFlashAttribute("message", "댓글이 수정되었습니다!");
            return "redirect:/posts/" + postId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/posts/" + postId;
        }
    }

    /**
     * 댓글 삭제
     */
    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                              @RequestParam String authorId,
                              @RequestParam Long postId,
                              RedirectAttributes redirectAttributes) {
        try {
            commentService.deleteComment(commentId, authorId);
            redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다!");
            return "redirect:/posts/" + postId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/posts/" + postId;
        }
    }

    /**
     * AJAX - 댓글 목록 조회 (JSON)
     */
    @GetMapping("/api/posts/{postId}/comments")
    @ResponseBody
    public Map<String, Object> getComments(@PathVariable Long postId) {
        try {
            var comments = commentService.getCommentsByPost(postId);
            var stats = commentService.getCommentStats(postId);
            
            return Map.of(
                "success", true,
                "comments", comments,
                "stats", stats
            );
        } catch (RuntimeException e) {
            return Map.of(
                "success", false,
                "message", e.getMessage()
            );
        }
    }

    /**
     * AJAX - 댓글 작성 (JSON)
     */
    @PostMapping("/api/posts/{postId}/comments")
    @ResponseBody
    public Map<String, Object> createCommentApi(@PathVariable Long postId,
                                              @RequestBody CommentDto commentDto) {
        try {
            commentDto.setPostId(postId);
            CommentDto savedComment = commentService.createComment(commentDto);
            
            return Map.of(
                "success", true,
                "message", "댓글이 작성되었습니다!",
                "comment", savedComment
            );
        } catch (RuntimeException e) {
            return Map.of(
                "success", false,
                "message", e.getMessage()
            );
        }
    }

    /**
     * AJAX - 대댓글 작성 (JSON)
     */
    @PostMapping("/api/posts/{postId}/comments/{parentId}/reply")
    @ResponseBody
    public Map<String, Object> createReplyApi(@PathVariable Long postId,
                                            @PathVariable Long parentId,
                                            @RequestBody CommentDto commentDto) {
        try {
            commentDto.setPostId(postId);
            commentDto.setParentId(parentId);
            CommentDto savedComment = commentService.createComment(commentDto);
            
            return Map.of(
                "success", true,
                "message", "답글이 작성되었습니다!",
                "comment", savedComment
            );
        } catch (RuntimeException e) {
            return Map.of(
                "success", false,
                "message", e.getMessage()
            );
        }
    }

    /**
     * AJAX - 댓글 수정 (JSON)
     */
    @PutMapping("/api/comments/{commentId}")
    @ResponseBody
    public Map<String, Object> updateCommentApi(@PathVariable Long commentId,
                                              @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            String authorId = request.get("authorId");
            
            CommentDto updatedComment = commentService.updateComment(commentId, content, authorId);
            
            return Map.of(
                "success", true,
                "message", "댓글이 수정되었습니다!",
                "comment", updatedComment
            );
        } catch (RuntimeException e) {
            return Map.of(
                "success", false,
                "message", e.getMessage()
            );
        }
    }

    /**
     * AJAX - 댓글 삭제 (JSON)
     */
    @DeleteMapping("/api/comments/{commentId}")
    @ResponseBody
    public Map<String, Object> deleteCommentApi(@PathVariable Long commentId,
                                              @RequestParam String authorId) {
        try {
            commentService.deleteComment(commentId, authorId);
            
            return Map.of(
                "success", true,
                "message", "댓글이 삭제되었습니다!"
            );
        } catch (RuntimeException e) {
            return Map.of(
                "success", false,
                "message", e.getMessage()
            );
        }
    }

    /**
     * 특정 사용자의 댓글 목록
     */
    @GetMapping("/users/{authorId}/comments")
    public String getUserComments(@PathVariable String authorId) {
        // TODO: 사용자 댓글 목록 페이지 구현
        return "comment/user-comments";
    }
}