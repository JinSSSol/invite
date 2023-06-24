package com.study.api.group.controller;

import static com.study.api.exception.ErrorCode.ALREADY_JOINED_USER;
import static com.study.api.exception.ErrorCode.NOT_FOUND_GROUP;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study.api.exception.CustomException;
import com.study.api.group.dto.JoinGroupDto;
import com.study.api.group.service.GroupJoinService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = GroupJoinController.class, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)})
class GroupJoinControllerTest {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private GroupRepository groupRepository;
    @MockBean
    private JoinGroupRepository joinGroupRepository;
    @MockBean
    private GroupJoinService groupJoinService;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @MockBean
    private EmbeddedRedisConfig embeddedRedisConfig;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("초대링크 가입 성공")
    void joinGroup_SUCCESS() throws Exception {
        // given
        given(groupJoinService.joinGroupByUrl(any()))
            .willReturn(new JoinGroupDto("test@abc.com", 1L, "manager@abc.com", false));

        // when
        // then
        mockMvc
            .perform(post("/groups/join?url=testUrl").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userEmail").value("test@abc.com"))
            .andExpect(jsonPath("$.groupId").value(1))
            .andExpect(jsonPath("$.groupName").value("manager@abc.com"))
            .andExpect(jsonPath("$.isManager").value(false));

    }

    @Test
    @WithMockUser
    @DisplayName("초대링크 가입 실패_이미 그룹 가인된 유저")
    void joinGroup_FAIL_ALREADY_JOINED() throws Exception {
        // given
        given(groupJoinService.joinGroupByUrl(any()))
            .willThrow(new CustomException(ALREADY_JOINED_USER));

        // when
        // then
        mockMvc.perform(post("/groups/join?url=testUrl").with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ALREADY_JOINED_USER"));
    }

    @Test
    @WithMockUser
    @DisplayName("초대링크 가입 실패_해당 그룹 없음")
    void joinGroup_FAIL_NOT_FOUND_GROUP() throws Exception {
        // given
        given(groupJoinService.joinGroupByUrl(any()))
            .willThrow(new CustomException(NOT_FOUND_GROUP));

        // when
        // then
        mockMvc.perform(post("/groups/join?url=testUrl").with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOT_FOUND_GROUP"));
    }
}