package com.study.api.group.controller;

import static com.study.api.exception.ErrorCode.ALREADY_JOINED_USER;
import static com.study.api.exception.ErrorCode.NOT_HAVE_MANAGER_AUTHORITY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.api.exception.CustomException;
import com.study.api.group.dto.InviteDto;
import com.study.api.group.dto.InviteForm;
import com.study.api.group.dto.InviteForm.Exist;
import com.study.api.group.dto.InviteForm.New;
import com.study.api.group.service.GroupInviteService;
import com.study.api.security.JwtAuthenticationFilter;
import com.study.domain.config.EmbeddedRedisConfig;
import com.study.domain.repository.GroupRepository;
import com.study.domain.repository.JoinGroupRepository;
import com.study.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = GroupInviteController.class, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)})
class GroupInviteControllerTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private GroupRepository groupRepository;
    @MockBean
    private JoinGroupRepository joinGroupRepository;

    @MockBean
    private GroupInviteService groupInviteService;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @MockBean
    private EmbeddedRedisConfig embeddedRedisConfig;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private final InviteForm.New newForm = New.builder()
        .userEmail("new@abc.com")
        .userName("newUser")
        .phone("01012341234")
        .build();
    private final InviteForm.Exist existForm = Exist.builder().userEmail("exist@abc.com").build();

    @Test
    @WithMockUser
    @DisplayName("신규회원 초대링크 생성 성공")
    void inviteNewUser_SUCCESS() throws Exception {
        // given
        given(groupInviteService.inviteNewUser(any(), any(), any()))
            .willReturn(new InviteDto("url", newForm.getUserName(), "manager@abc.com"));

        // when
        // then
        mockMvc
            .perform(post("/groups/invite/new/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newForm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.urlCode").value("url"));
    }

    @Test
    @WithMockUser
    @DisplayName("신규회원 초대링크 생성 실패_매니저 X")
    void inviteNewUser_FAIL_NOT_MANAGER() throws Exception {
        // given
        given(groupInviteService.inviteNewUser(any(), any(), any()))
            .willThrow(new CustomException(NOT_HAVE_MANAGER_AUTHORITY));

        // when
        // then
        mockMvc.perform(post("/groups/invite/new/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newForm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOT_HAVE_MANAGER_AUTHORITY"));
    }

    @Test
    @WithMockUser
    @DisplayName("기존회원 초대링크 생성 성공")
    void inviteExistUser_SUCCESS() throws Exception {
        // given
        given(groupInviteService.inviteExistUser(any(), any(), any()))
            .willReturn(new InviteDto("url", existForm.getUserEmail(), "manager@abc.com"));

        // when
        // then
        mockMvc
            .perform(post("/groups/invite/exist/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existForm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.urlCode").value("url"));

    }

    @Test
    @WithMockUser
    @DisplayName("기존회원 초대링크 생성 실패_이미 그룹 가입된 유저")
    void inviteExistUser_FAIL_ALREADY_JOINED() throws Exception {
        // given
        given(groupInviteService.inviteExistUser(any(), any(), any()))
            .willThrow(new CustomException(ALREADY_JOINED_USER));

        // when
        // then
        mockMvc.perform(post("/groups/invite/exist/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existForm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ALREADY_JOINED_USER"));
    }
}