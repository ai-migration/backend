package backend.infra;

import backend.domain.*;
import backend.dto.PostDetailResponseDto;
import backend.dto.PostListFaqResponseDto;
import backend.dto.PostListNoticeResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/posts")
@RequiredArgsConstructor
@Transactional
public class PostController {

    private final PostReadModelRepository postReadModelRepository;

    // 1. 목록 조회 (FAQ / NOTICE)
    @GetMapping
    public ResponseEntity<?> getPostList(@RequestParam("type") String type) {
        PostType postType = PostType.valueOf(type.toUpperCase());

        List<PostReadModel> posts = postReadModelRepository.findByType(postType);

        if(postType == PostType.FAQ){
            List<PostListFaqResponseDto> faqList = posts.stream()
                .map(p -> new PostListFaqResponseDto(
                    p.getPostId(), 
                    p.getTitle(), 
                    p.getContent()
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(faqList);
        } else if(postType == PostType.NOTICE){
            List<PostListNoticeResponseDto> noticeList = posts.stream()
                .map(p -> new PostListNoticeResponseDto(
                    p.getPostId(), 
                    p.getTitle(), 
                    p.getType(),
                    p.getCreatedAt(),
                    p.getViewCount()
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(noticeList);
        }
        return ResponseEntity.badRequest().body("지원하지 않는 type입니다.");

    }

    // 2. 단건 조회 + viewCount 증가
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDto> getPostDetail(
        @PathVariable Long postId,
        @RequestParam("type") String type) {
        PostReadModel post = postReadModelRepository.findByPostId(postId)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if(type.equalsIgnoreCase("notice")){
            post.setViewCount(post.getViewCount() + 1);
            postReadModelRepository.save(post);
        }

        PostDetailResponseDto response = new PostDetailResponseDto(
            post.getPostId(),
            post.getTitle(),
            post.getContent(),
            post.getType(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            post.getViewCount()
        );

        return ResponseEntity.ok(response);

    }
    
    


}
//>>> Clean Arch / Inbound Adaptor
