package com.study.api.group.service;


import static com.study.api.exception.ErrorCode.NOT_FOUND_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.study.api.exception.CustomException;
import com.study.api.group.dto.GroupDto;
import com.study.api.group.dto.GroupDto.Request;
import com.study.domain.model.Group;
import com.study.domain.model.JoinGroup;
import com.study.domain.model.User;
import com.study.domain.repository.GroupRepository;
import com.study.domain.repository.JoinGroupRepository;
import com.study.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private JoinGroupRepository joinGroupRepository;
    @InjectMocks
    private GroupService groupService;

    private final GroupDto.Request request = new Request("testGroup");

    @Test
    @DisplayName("그룹생성 성공")
    void add_SUCCESS() {
        // given
        User user = User.builder()
            .userEmail("test@abc.com")
            .userName("테스트")
            .password("1234")
            .phone("00012341234")
            .build();

        Group group = Group.builder()
            .id(1L)
            .groupName("testGroup")
            .build();

        JoinGroup joinGroup = JoinGroup.builder()
            .id(1L)
            .group(group)
            .user(user)
            .build();

        given(userRepository.findByUserEmail("test@abc.com"))
            .willReturn(Optional.of(user));

        given(groupRepository.save(any()))
            .willReturn(group);

        given(joinGroupRepository.save(any()))
            .willReturn(joinGroup);

        // when
        GroupDto.Response response = groupService.add(request, "test@abc.com");
        ArgumentCaptor<JoinGroup> captor = ArgumentCaptor.forClass(JoinGroup.class);

        // then
        verify(joinGroupRepository, times(1)).save(captor.capture());
        assertTrue(captor.getValue().getIsManager());
        assertTrue(captor.getValue().getIsActive());
        assertEquals(response.getId(), 1L);
        assertEquals(response.getGroupName(), "testGroup");
    }

    @Test
    @DisplayName("그룹생성 실패_해당 회원 X")
    void add_FAIL() {
        // given
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> groupService.add(request, "test@abc.com"));

        // then
        assertEquals(exception.getErrorCode(), NOT_FOUND_USER);
    }
}