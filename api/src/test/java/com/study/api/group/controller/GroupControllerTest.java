package com.study.api.group.controller;

import static com.study.api.exception.ErrorCode.NOT_FOUND_USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.api.exception.CustomException;
import com.study.api.group.dto.GroupDto;
import com.study.api.group.dto.GroupDto.Request;
import com.study.api.group.service.GroupService;
import com.study.api.security.JwtAuthenticationFilter;
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

@WebMvcTest(value = GroupController.class, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)})
class GroupControllerTest {

    @MockBean
    private GroupService groupService;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private GroupRepository groupRepository;
    @MockBean
    private JoinGroupRepository joinGroupRepository;
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private final GroupDto.Request request = new Request("testGroup");

    @Test
    @WithMockUser
    @DisplayName("그룹 생성 성공")
    void makeGroup_SUCCESS() throws Exception {
        // given
        given(groupService.add(any(), any()))
            .willReturn(new GroupDto.Response(1L, "testGroup"));

        // when
        // then
        mockMvc
            .perform(post("/groups").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.groupName").value("testGroup"));
    }

    @Test
    @WithMockUser
    @DisplayName("그룹 생성 실패_해당 회원 X")
    void makeGroup_FAIL() throws Exception {
        // given
        given(groupService.add(any(), any()))
            .willThrow(new CustomException(NOT_FOUND_USER));

        // when
        // then
        mockMvc.perform(post("/groups").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(jsonPath("$.code").value("NOT_FOUND_USER"));

    }
}