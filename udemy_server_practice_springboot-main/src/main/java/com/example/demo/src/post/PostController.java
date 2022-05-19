package com.example.demo.src.post;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.config.BaseResponseStatus;
import com.example.demo.src.post.model.*;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final PostProvider postProvider;
    @Autowired
    private final PostService postService;
    @Autowired
    private final JwtService jwtService;




    public PostController(PostProvider postProvider, PostService postService, JwtService jwtService){
        this.postProvider = postProvider;
        this.postService = postService;
        this.jwtService = jwtService;
    }


    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetPostsRes>> getPosts(@RequestParam int userIdx) {
        try{

            List<GetPostsRes> getPostsRes = postProvider.retrievePosts(userIdx);
            return new BaseResponse<>(getPostsRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //Post method이기 때문에 body값 필요
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostPostsRes> createPosts(@RequestBody PostPostsReq postPostsReq) {
        try{
            //config-BaseResponseStatus에 에러코드 추가해준 후, validation처리
            if(postPostsReq.getContent().length()>450) {
                return new BaseResponse<>(BaseResponseStatus.POST_POSTS_INVALID_CONTENTS);
            }
            if(postPostsReq.getPostImgUrls().size()<1) {
                return new BaseResponse<>(BaseResponseStatus.POST_POSTS_EMPTY_IMGURL);
            }
            //무언가를 생성하기 때문에 postService에서 처리, body에서 userIdx빼온 이유는 나중에 JWT에서 받아올 수 있기 때문
            PostPostsRes postPostsRes = postService.createPosts(postPostsReq.getUserIdx(),postPostsReq);
           return new BaseResponse<>(postPostsRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @PatchMapping("/{postIdx}")
    public BaseResponse<String> modifyPost(@PathVariable ("postIdx") int postIdx, @RequestBody PatchPostReq patchPostReq) {
        try{
            if(patchPostReq.getContent().length()>450) {
                return new BaseResponse<>(BaseResponseStatus.POST_POSTS_INVALID_CONTENTS);
            }
            postService.modifyPost(patchPostReq.getUserIdx(), postIdx, patchPostReq);
            String result = "게시글 수정을 완료하였습니다.";
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @PatchMapping("/{postIdx}/status")
    public BaseResponse<String> deletePost(@PathVariable ("postIdx") int postIdx) {
        try{
            postService.deletePost(postIdx);
            String result = "게시글 삭제를 완료하였습니다.";
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
