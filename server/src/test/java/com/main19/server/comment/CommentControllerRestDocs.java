package com.main19.server.comment;

import static com.main19.server.utils.DocumentUtils.getRequestPreProcessor;
import static com.main19.server.utils.DocumentUtils.getResponsePreProcessor;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.main19.server.domain.comment.controller.CommentController;
import com.main19.server.domain.comment.dto.CommentDto;
import com.main19.server.domain.comment.dto.CommentDto.Patch;
import com.main19.server.domain.comment.dto.CommentDto.Post;
import com.main19.server.domain.comment.entity.Comment;
import com.main19.server.domain.comment.mapper.CommentMapper;
import com.main19.server.domain.comment.service.CommentService;
import com.main19.server.domain.member.entity.Member;
import com.main19.server.domain.posting.entity.Posting;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(value = CommentController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs(uriHost = "increaf.site")
public class CommentControllerRestDocs {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;
    @MockBean
    private CommentMapper commentMapper;
    @Autowired
    private Gson gson;

    @Test
    public void PostCommentTest() throws Exception {

        long postingId = 1L;
        long memberId = 1L;
        long commentId = 1L;

        CommentDto.Post post = new Post(memberId, "?????? test");

        String content = gson.toJson(post);

        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime modifiedAt = LocalDateTime.now();

        CommentDto.Response response =
            new CommentDto.Response(
                commentId,
                postingId,
                memberId,
                "oheadnah",
                "image",
                "?????? test",
                0L,
                new ArrayList<>(),
                createdAt,
                modifiedAt);

        given(commentMapper.commentsPostDtoToComments(Mockito.any(CommentDto.Post.class)))
            .willReturn(new Comment());

        given(commentService.createComment(Mockito.any(Comment.class), Mockito.anyLong(),
            Mockito.anyLong(), Mockito.anyString()))
            .willReturn(new Comment());

        given(commentMapper.commentsToCommentsResponseDto(Mockito.any(Comment.class))).willReturn(
            response);

        ResultActions actions =
            mockMvc.perform(
                post("/comments/{posting-id}", postingId)
                    .header("Authorization", "Bearer AccessToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
            );

        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.memberId").value(post.getMemberId()))
            .andExpect(jsonPath("$.data.comment").value(post.getComment()))
            .andDo(document(
                "post-comment",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer AccessToken")
                ),
                pathParameters(
                    parameterWithName("posting-id").description("????????? ?????????")
                ),
                requestFields(
                    List.of(
                        fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                        fieldWithPath("comment").type(JsonFieldType.STRING).description("?????? ??????")
                    )
                ),
                responseFields(
                    fieldWithPath("data.commentId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                    fieldWithPath("data.postingId").type(JsonFieldType.NUMBER).description("????????? ?????????"),
                    fieldWithPath("data.memberId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                    fieldWithPath("data.userName").type(JsonFieldType.STRING).description("?????? ?????????"),
                    fieldWithPath("data.profileImage").type(JsonFieldType.STRING).description("?????? ?????????"),
                    fieldWithPath("data.comment").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.likeCount").type(JsonFieldType.NUMBER).description("????????? ??????"),
                    fieldWithPath("data.commentLikeList").type(JsonFieldType.ARRAY).description("????????? ?????? ??????"),
                    fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("?????????"),
                    fieldWithPath("data.modifiedAt").type(JsonFieldType.STRING).description("?????? ?????????")
                )
            ));
    }

    @Test
    public void patchCommentTest() throws Exception {

        long postingId = 1L;
        long memberId = 1L;
        long commentId = 1L;

        CommentDto.Patch patch = new Patch(commentId, "?????? ?????? test");

        String content = gson.toJson(patch);

        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime modifiedAt = LocalDateTime.now();

        CommentDto.Response response =
            new CommentDto.Response(
                commentId,
                postingId,
                memberId,
                "oheadnah",
                "image",
                "?????? ?????? test",
                0L,
                new ArrayList<>(),
                createdAt,
                modifiedAt);

        given(commentMapper.commentsPatchDtoToComments(Mockito.any(CommentDto.Patch.class)))
            .willReturn(new Comment());

        given(commentService.updateComment(Mockito.any(Comment.class), Mockito.anyString()))
            .willReturn(new Comment());

        given(commentMapper.commentsToCommentsResponseDto(Mockito.any(Comment.class))).willReturn(
            response);

        ResultActions actions =
            mockMvc.perform(
                patch("/comments/{comment-id}", commentId)
                    .header("Authorization", "Bearer AccessToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
            );

        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.commentId").value(patch.getCommentId()))
            .andExpect(jsonPath("$.data.comment").value(patch.getComment()))
            .andDo(document(
                "patch-comment",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer AccessToken")
                ),
                pathParameters(
                    parameterWithName("comment-id").description("?????? ?????????")
                ),
                requestFields(
                    List.of(
                        fieldWithPath("commentId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                        fieldWithPath("comment").type(JsonFieldType.STRING).description("?????? ??????")
                    )
                ),
                responseFields(
                    fieldWithPath("data.commentId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                    fieldWithPath("data.postingId").type(JsonFieldType.NUMBER).description("????????? ?????????"),
                    fieldWithPath("data.memberId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                    fieldWithPath("data.userName").type(JsonFieldType.STRING).description("?????? ?????????"),
                    fieldWithPath("data.profileImage").type(JsonFieldType.STRING).description("?????? ?????????"),
                    fieldWithPath("data.comment").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.likeCount").type(JsonFieldType.NUMBER).description("????????? ??????"),
                    fieldWithPath("data.commentLikeList").type(JsonFieldType.ARRAY).description("????????? ?????? ??????"),
                    fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("?????????"),
                    fieldWithPath("data.modifiedAt").type(JsonFieldType.STRING).description("?????? ?????????")
                )
            ));
    }

    @Test
    public void GetPostingCommentsTest() throws Exception {

        long postingId = 1L;
        long commentId1 = 1L;
        long commentId2 = 2L;
        long memberId = 1L;

        Member member = new Member();
        member.setMemberId(memberId);
        member.setUserName("??????");
        member.setProfileImage("profileImage");

        Posting posting= new Posting();
        posting.setPostingId(postingId);

        Comment comment1 = new Comment(commentId1,"?????? ??? ??????1",LocalDateTime.now(),LocalDateTime.now(),0L,posting,member,new ArrayList<>());
        Comment comment2 = new Comment(commentId2,"?????? ??? ??????2",LocalDateTime.now(),LocalDateTime.now(),0L,posting,member,new ArrayList<>());

        Page<Comment> pageComments = new PageImpl<>(List.of(comment1, comment2));
        List<Comment> listComments = List.of(comment1,comment2);

        given(commentService.findComments(Mockito.anyLong(),Mockito.anyInt(),Mockito.anyInt()))
            .willReturn(pageComments);

        given(commentMapper.commentsToCommentsResponseDtos(Mockito.anyList())).willReturn(
            List.of(
                new CommentDto.Response(
                    listComments.get(0).getCommentId(),
                    listComments.get(0).getPosting().getPostingId(),
                    listComments.get(0).getMember().getMemberId(),
                    listComments.get(0).getMember().getUserName(),
                    listComments.get(0).getMember().getProfileImage(),
                    listComments.get(0).getComment(),
                    listComments.get(0).getLikeCount(),
                    new ArrayList<>(),
                    listComments.get(0).getCreatedAt(),
                    listComments.get(0).getModifiedAt()),
                new CommentDto.Response(
                    listComments.get(1).getCommentId(),
                    listComments.get(1).getPosting().getPostingId(),
                    listComments.get(1).getMember().getMemberId(),
                    listComments.get(1).getMember().getUserName(),
                    listComments.get(1).getMember().getProfileImage(),
                    listComments.get(1).getComment(),
                    listComments.get(1).getLikeCount(),
                    new ArrayList<>(),
                    listComments.get(0).getCreatedAt(),
                    listComments.get(0).getModifiedAt())));

        ResultActions actions =
            mockMvc.perform(
                get("/comments/{posting-id}", postingId)
                    .param("page","1")
                    .param("size","10")
                    .header("Authorization", "Bearer AccessToken")
                    .accept(MediaType.APPLICATION_JSON)
            );

        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].commentId").value(listComments.get(0).getCommentId()))
            .andExpect(jsonPath("$.data[0].postingId").value(listComments.get(0).getPosting().getPostingId()))
            .andExpect(jsonPath("$.data[0].memberId").value(listComments.get(0).getMember().getMemberId()))
            .andExpect(jsonPath("$.data[0].userName").value(listComments.get(0).getMember().getUserName()))
            .andExpect(jsonPath("$.data[0].profileImage").value(listComments.get(0).getMember().getProfileImage()))
            .andExpect(jsonPath("$.data[0].comment").value(listComments.get(0).getComment()))
            .andExpect(jsonPath("$.data[0].likeCount").value(listComments.get(0).getLikeCount()))
            .andExpect(jsonPath("$.data[0].commentLikeList").value(listComments.get(0).getCommentLikeList()))
            .andExpect(jsonPath("$.data[1].commentId").value(listComments.get(1).getCommentId()))
            .andExpect(jsonPath("$.data[1].postingId").value(listComments.get(1).getPosting().getPostingId()))
            .andExpect(jsonPath("$.data[1].memberId").value(listComments.get(1).getMember().getMemberId()))
            .andExpect(jsonPath("$.data[1].userName").value(listComments.get(1).getMember().getUserName()))
            .andExpect(jsonPath("$.data[1].profileImage").value(listComments.get(1).getMember().getProfileImage()))
            .andExpect(jsonPath("$.data[1].comment").value(listComments.get(1).getComment()))
            .andExpect(jsonPath("$.data[1].likeCount").value(listComments.get(1).getLikeCount()))
            .andExpect(jsonPath("$.data[1].commentLikeList").value(listComments.get(1).getCommentLikeList()))


            .andDo(document(
                "gets-comments",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer AccessToken")
                ),
                requestParameters(
                    parameterWithName("page").description("?????? ??? ?????????"),
                    parameterWithName("size").description("?????? ??? ????????? ??????")
                ),
                pathParameters(
                    parameterWithName("posting-id").description("????????? ?????????")
                ),
                responseFields(
                    fieldWithPath("data[].commentId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                    fieldWithPath("data[].postingId").type(JsonFieldType.NUMBER).description("????????? ?????????"),
                    fieldWithPath("data[].memberId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                    fieldWithPath("data[].userName").type(JsonFieldType.STRING).description("?????? ?????????"),
                    fieldWithPath("data[].profileImage").type(JsonFieldType.STRING).description("?????? ????????? ?????????"),
                    fieldWithPath("data[].comment").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data[].likeCount").type(JsonFieldType.NUMBER).description("????????? ??????"),
                    fieldWithPath("data[].commentLikeList").type(JsonFieldType.ARRAY).description("?????? ????????? ?????????"),
                    fieldWithPath("data[].createdAt").type(JsonFieldType.STRING).description("?????? ?????????"),
                    fieldWithPath("data[].modifiedAt").type(JsonFieldType.STRING).description("?????? ?????????"),
                    fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("????????? ??????"),
                    fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                    fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("????????? ?????????"),
                    fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                    fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("?????? ????????? ???")
                )
            ));
    }

    @Test
    public void deleteCommentTest() throws Exception {

        long commentId = 1L;

        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime modifiedAt = LocalDateTime.now();

        Member member = new Member();

        Posting posting = new Posting();

        Comment comment = new Comment(commentId, "?????? ?????? test", createdAt, modifiedAt, 0L, posting, member, new ArrayList<>());

        doNothing().when(commentService).deleteComment(comment.getCommentId(), "token");

        ResultActions actions =
            mockMvc.perform(
                delete("/comments/{comment-id}", commentId)
                    .header("Authorization", "Bearer AccessToken")
            );

        actions.andExpect(status().isNoContent())
            .andDo(
                document(
                    "delete-comment",
                    getRequestPreProcessor(),
                    getResponsePreProcessor(),
                    pathParameters(parameterWithName("comment-id").description("?????? ?????????")
                    ),
                    requestHeaders(
                        headerWithName("Authorization").description("Bearer (accessToken)")
                    )
                )
            );
    }
}
