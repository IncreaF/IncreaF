package com.main19.server.myplants;

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
import com.main19.server.domain.member.entity.Member;
import com.main19.server.domain.myplants.controller.MyPlantsController;
import com.main19.server.domain.myplants.dto.MyPlantsDto;
import com.main19.server.domain.myplants.dto.MyPlantsDto.Patch;
import com.main19.server.domain.myplants.dto.MyPlantsDto.PlantsPatch;
import com.main19.server.domain.myplants.dto.MyPlantsDto.Post;
import com.main19.server.domain.myplants.dto.MyPlantsDto.Response;
import com.main19.server.domain.myplants.entity.MyPlants;
import com.main19.server.domain.myplants.gallery.dto.GalleryDto;
import com.main19.server.domain.myplants.mapper.MyPlantsMapper;
import com.main19.server.domain.myplants.service.MyPlantsService;
import com.main19.server.global.storageService.s3.GalleryStorageService;

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

@WebMvcTest(value = MyPlantsController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs(uriHost = "increaf.site")
public class MyPlantsControllerRestDocs {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Gson gson;
    @MockBean
    private MyPlantsMapper myPlantsMapper;
    @MockBean
    private MyPlantsService myPlantsService;
    @MockBean
    private GalleryStorageService storageService;


    @Test
    public void PostMyPlantsTest() throws Exception {

        long myPlantsId = 1L;
        long memberId = 1L;

        MyPlantsDto.Post post = new Post(memberId, "??????","??????","2023.01.21");

        String content = gson.toJson(post);

        MyPlantsDto.Response response = new Response(myPlantsId, "??????", "??????", "2023.01.21", new ArrayList<>());

        given(myPlantsMapper.myPlantsPostDtoToMyPlants(Mockito.any(MyPlantsDto.Post.class)))
            .willReturn(new MyPlants());

        given(myPlantsService.createMyPlants(Mockito.any(MyPlants.class), Mockito.anyLong(),
            Mockito.anyString()))
            .willReturn(new MyPlants());

        given(myPlantsMapper.myPlantsToMyPlantsResponseDto(Mockito.any(MyPlants.class))).willReturn(
            response);

        ResultActions actions =
            mockMvc.perform(
                post("/myplants")
                    .header("Authorization", "Bearer AccessToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
            );

        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.plantName").value(post.getPlantName()))
            .andExpect(jsonPath("$.data.plantType").value(post.getPlantType()))
            .andExpect(jsonPath("$.data.plantBirthDay").value(post.getPlantBirthDay()))
            .andDo(document(
                "post-my-plants",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer AccessToken")
                ),
                requestFields(
                    List.of(
                        fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                        fieldWithPath("plantName").type(JsonFieldType.STRING).description("?????? ??????"),
                        fieldWithPath("plantType").type(JsonFieldType.STRING).description("?????? ??????"),
                        fieldWithPath("plantBirthDay").type(JsonFieldType.STRING).description("?????? ??????")
                    )
                ),
                responseFields(
                    fieldWithPath("data.myPlantsId").type(JsonFieldType.NUMBER).description("??? ?????? ?????????"),
                    fieldWithPath("data.plantName").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.plantType").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.plantBirthDay").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.galleryList").type(JsonFieldType.ARRAY).description("????????? ?????????")
                )
            ));
    }

    @Test
    public void patchMyPlantsTest() throws Exception {

        long myPlantsId = 1L;
        long galleryId1 = 1L;
        long galleryId2 = 2L;
        int changeNumber = 2;

        MyPlantsDto.Patch patch = new Patch(galleryId1, changeNumber);

        GalleryDto.MyPlantsResponse response1 = new GalleryDto.MyPlantsResponse(galleryId1);
        GalleryDto.MyPlantsResponse response2 = new GalleryDto.MyPlantsResponse(galleryId2);

        List<GalleryDto.MyPlantsResponse> responses = new ArrayList<>();
        responses.add(response2);
        responses.add(response1);

        String content = gson.toJson(patch);

        MyPlantsDto.Response response = new Response(myPlantsId, "??????","??????","2023.01.21", responses);

        given(myPlantsService.changeMyPlants(Mockito.anyLong(), Mockito.anyLong(),
            Mockito.anyInt(), Mockito.anyString()))
            .willReturn(new MyPlants());

        given(myPlantsMapper.myPlantsToMyPlantsResponseDto(Mockito.any(MyPlants.class))).willReturn(
            response);

        ResultActions actions =
            mockMvc.perform(
                patch("/myplants/{myplants-id}/gallerys", myPlantsId)
                    .header("Authorization", "Bearer AccessToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
            );

        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.galleryList[1].galleryId").value(patch.getGalleryId()))
            .andDo(document(
                "patch-my-plants",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer AccessToken")
                ),
                pathParameters(
                    parameterWithName("myplants-id").description("??? ?????? ?????????")
                ),
                requestFields(
                    List.of(
                        fieldWithPath("galleryId").type(JsonFieldType.NUMBER).description("????????? ?????????"),
                        fieldWithPath("changeNumber").type(JsonFieldType.NUMBER).description("???????????? ??????")
                    )
                ),
                responseFields(
                    fieldWithPath("data.myPlantsId").type(JsonFieldType.NUMBER).description("??? ?????? ?????????"),
                    fieldWithPath("data.plantName").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.plantType").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.plantBirthDay").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.galleryList[].galleryId").type(JsonFieldType.NUMBER).description("????????? ?????????")
                )
            ));
    }

    @Test
    public void PatchMyPlantTest() throws Exception {

        long myPlantsId = 1L;

        MyPlantsDto.PlantsPatch patch = new PlantsPatch(myPlantsId,"??????2","??????2","2023.02.01");

        MyPlantsDto.Response response = new Response(myPlantsId, "??????2","??????2","2023.02.01", new ArrayList<>());

        String content = gson.toJson(patch);

        given(myPlantsMapper.myPlantsPatchDtoToMyPlants(Mockito.any())).willReturn(new MyPlants());
        given(myPlantsService.updateMyPlants(Mockito.any(),Mockito.anyString())).willReturn(new MyPlants());
        given(myPlantsMapper.myPlantsToMyPlantsResponseDto(Mockito.any())).willReturn(response);

        ResultActions actions =
            mockMvc.perform(
                patch("/myplants/{myplants-id}", myPlantsId)
                    .header("Authorization", "Bearer AccessToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
            );

        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.plantName").value(patch.getPlantName()))
            .andExpect(jsonPath("$.data.plantType").value(patch.getPlantType()))
            .andExpect(jsonPath("$.data.plantBirthDay").value(patch.getPlantBirthDay()))
            .andDo(document(
                "patch-my-plant",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer AccessToken")
                ),
                pathParameters(
                    parameterWithName("myplants-id").description("??? ?????? ?????????")
                ),
                requestFields(
                    List.of(
                        fieldWithPath("myPlantsId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                        fieldWithPath("plantName").type(JsonFieldType.STRING).description("?????? ??????").optional(),
                        fieldWithPath("plantType").type(JsonFieldType.STRING).description("?????? ??????").optional(),
                        fieldWithPath("plantBirthDay").type(JsonFieldType.STRING).description("?????? ??????").optional()
                    )
                ),
                responseFields(
                    fieldWithPath("data.myPlantsId").type(JsonFieldType.NUMBER).description("??? ?????? ?????????"),
                    fieldWithPath("data.plantName").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.plantType").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.plantBirthDay").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.galleryList[]").type(JsonFieldType.ARRAY).description("????????? ?????????")
                )
            ));
    }

    @Test
    public void GetMyPlantsTest() throws Exception {

        long myPlantsId = 1L;

        MyPlantsDto.Response response = new MyPlantsDto.Response(myPlantsId,"??????","??????","2023.01.21",new ArrayList<>());

        given(myPlantsService.findMyPlants(Mockito.anyLong())).willReturn(new MyPlants());
        given(myPlantsMapper.myPlantsToMyPlantsResponseDto(Mockito.any())).willReturn(response);

        ResultActions actions =
            mockMvc.perform(
                get("/myplants/{myplants-id}", myPlantsId)
                    .accept(MediaType.APPLICATION_JSON)
            );

        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.myPlantsId").value(response.getMyPlantsId()))
            .andExpect(jsonPath("$.data.plantName").value(response.getPlantName()))
            .andExpect(jsonPath("$.data.galleryList").value(response.getGalleryList()))

            .andDo(document(
                "get-my-plants",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                pathParameters(
                    parameterWithName("myplants-id").description("??? ?????? ?????????")
                ),
                responseFields(
                    fieldWithPath("data.myPlantsId").type(JsonFieldType.NUMBER).description("??? ?????? ?????????"),
                    fieldWithPath("data.plantName").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.plantType").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.plantBirthDay").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data.galleryList").type(JsonFieldType.ARRAY).description("????????? ?????????")
                )
            ));
    }

    @Test
    public void GetsMyPlantsTest() throws Exception {

        long plantsId1 = 1L;
        long plantsId2 = 2L;
        long memberId = 1L;

        Member member = new Member();
        member.setMemberId(memberId);

        MyPlants myPlants1 = new MyPlants(plantsId1,"??????","??????","2023.01.21",member,new ArrayList<>());
        MyPlants myPlants2 = new MyPlants(plantsId2,"??????","??????","2023.01.21",member,new ArrayList<>());

        Page<MyPlants> pageMyPlants = new PageImpl<>(List.of(myPlants1, myPlants2));
        List<MyPlants> listMyPlants = List.of(myPlants1,myPlants2);

        given(myPlantsService.findByMyPlants(Mockito.anyInt(),Mockito.anyInt(),Mockito.anyLong()))
            .willReturn(pageMyPlants);

        given(myPlantsMapper.myPlantsListToMyPlantsResponseDto(Mockito.anyList())).willReturn(
            List.of(
                new MyPlantsDto.Response(
                    listMyPlants.get(0).getMyPlantsId(),
                    listMyPlants.get(0).getPlantName(),
                    listMyPlants.get(0).getPlantType(),
                    listMyPlants.get(0).getPlantBirthDay(),
                    new ArrayList<>()),
                new MyPlantsDto.Response(
                    listMyPlants.get(1).getMyPlantsId(),
                    listMyPlants.get(1).getPlantName(),
                    listMyPlants.get(0).getPlantType(),
                    listMyPlants.get(0).getPlantBirthDay(),
                    new ArrayList<>())));

        ResultActions actions =
            mockMvc.perform(
                get("/{member-id}/myplants", memberId)
                    .param("page","1")
                    .param("size","10")
                    .accept(MediaType.APPLICATION_JSON)
            );

        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].myPlantsId").value(listMyPlants.get(0).getMyPlantsId()))
            .andExpect(jsonPath("$.data[0].plantName").value(listMyPlants.get(0).getPlantName()))
            .andExpect(jsonPath("$.data[0].plantType").value(listMyPlants.get(0).getPlantType()))
            .andExpect(jsonPath("$.data[0].plantBirthDay").value(listMyPlants.get(0).getPlantBirthDay()))
            .andExpect(jsonPath("$.data[0].galleryList").value(listMyPlants.get(0).getGalleryList()))
            .andExpect(jsonPath("$.data[1].myPlantsId").value(listMyPlants.get(1).getMyPlantsId()))
            .andExpect(jsonPath("$.data[1].plantName").value(listMyPlants.get(1).getPlantName()))
            .andExpect(jsonPath("$.data[1].plantType").value(listMyPlants.get(1).getPlantType()))
            .andExpect(jsonPath("$.data[1].plantBirthDay").value(listMyPlants.get(1).getPlantBirthDay()))
            .andExpect(jsonPath("$.data[1].galleryList").value(listMyPlants.get(1).getGalleryList()))

            .andDo(document(
                "gets-my-plants",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                pathParameters(
                    parameterWithName("member-id").description("?????? ?????????")
                ),
                requestParameters(
                        parameterWithName("page").description("?????? ??? ?????????"),
                        parameterWithName("size").description("?????? ??? ????????? ??????")
                ),
                responseFields(
                    fieldWithPath("data[].myPlantsId").type(JsonFieldType.NUMBER).description("??? ?????? ?????????"),
                    fieldWithPath("data[].plantName").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data[].plantType").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data[].plantBirthDay").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("data[].galleryList").type(JsonFieldType.ARRAY).description("????????? ?????????"),
                    fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("????????? ??????"),
                    fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                    fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("????????? ?????????"),
                    fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                    fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("?????? ????????? ???")
                )
            ));
    }

        @Test
        public void deleteMyPlantsTest() throws Exception {

            long myPlantsId = 1L;

            doNothing().when(storageService).removeAllGalleryImage(Mockito.anyLong(),Mockito.anyString());
            doNothing().when(myPlantsService).deleteMyPlants(Mockito.anyLong(),Mockito.anyString());

            ResultActions actions =
                mockMvc.perform(
                    delete("/myplants/{myplants-id}", myPlantsId)
                        .header("Authorization", "Bearer AccessToken")
                );

            actions.andExpect(status().isNoContent())
                .andDo(
                    document(
                        "delete-my-plants",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(
                            parameterWithName("myplants-id").description("??? ?????? ?????????")
                        ),
                        requestHeaders(
                            headerWithName("Authorization").description("Bearer (accessToken)")
                        )
                    )
                );
    }
}
