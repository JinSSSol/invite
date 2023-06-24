package com.study.api.group.service;

import static com.study.api.exception.ErrorCode.ALREADY_JOINED_USER;
import static com.study.api.exception.ErrorCode.NOT_FOUND_GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.study.api.client.RedisClient;
import com.study.api.exception.CustomException;
import com.study.api.group.dto.JoinGroupDto;
import com.study.api.group.dto.redis.Invite;
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
class GroupJoinServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private JoinGroupRepository joinGroupRepository;
    @Mock
    private RedisClient redisClient;
    @InjectMocks
    private GroupJoinService groupJoinService;

    private final String url = "testUrl";
    private final Invite invite = new Invite("test@abc.com", "test", "00012341234",
        "manager@abc.com", 1L);

    @Test
    @DisplayName("초대링크 그룹가입 성공_신규 회원")
    void joinGroupByUrl_SUCCESS_NEW_USER() {
        // given
        User user = User.builder().userEmail("test@abc.com").build();
        Group group = Group.builder().id(1L).groupName("test").build();
        JoinGroup joinGroup = JoinGroup.builder().user(user).group(group).build();

        given(redisClient.get(any(), any()))
            .willReturn(invite);

        given(userRepository.save(any()))
            .willReturn(user);

        given(groupRepository.findById(any()))
            .willReturn(Optional.of(group));

        given(joinGroupRepository.save(any()))
            .willReturn(joinGroup);

        // when
        JoinGroupDto response = groupJoinService.joinGroupByUrl(url);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<JoinGroup> joinGroupCaptor = ArgumentCaptor.forClass(JoinGroup.class);

        // then
        verify(userRepository, times(1)).save(userCaptor.capture());
        verify(joinGroupRepository, times(1)).save(joinGroupCaptor.capture());
        assertEquals(userCaptor.getValue().getUserEmail(),
            joinGroupCaptor.getValue().getUser().getUserEmail());
        assertEquals(response.getGroupName(), "test");
        assertEquals(response.getUserEmail(), "test@abc.com");
    }

    @Test
    @DisplayName("초대링크 그룹가입 성공_기존 회원")
    void joinGroupByUrl_SUCCESS_EXIST_USER() {
        // given
        User user = User.builder().userEmail("test@abc.com").build();
        Group group = Group.builder().id(1L).groupName("test").build();
        JoinGroup joinGroup = JoinGroup.builder().user(user).group(group).build();

        given(redisClient.get(any(), any()))
            .willReturn(invite);

        given(userRepository.findByUserEmail(any()))
            .willReturn(Optional.of(user));

        given(groupRepository.findById(any()))
            .willReturn(Optional.of(group));

        given(joinGroupRepository.save(any()))
            .willReturn(joinGroup);

        // when
        JoinGroupDto response = groupJoinService.joinGroupByUrl(url);
        ArgumentCaptor<String> userEmailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> groupIdCaptor = ArgumentCaptor.forClass(Long.class);

        // then
        verify(userRepository, times(0)).save(any());
        verify(userRepository, times(1)).findByUserEmail(userEmailCaptor.capture());
        verify(groupRepository, times(1)).findById(groupIdCaptor.capture());
        assertEquals(userEmailCaptor.getValue(), "test@abc.com");
        assertEquals(groupIdCaptor.getValue(), 1);
        assertEquals(response.getGroupName(), "test");
        assertEquals(response.getUserEmail(), "test@abc.com");
    }

    @Test
    @DisplayName("초대링크 그룹가입 실패_이미 그룹가입된 유저")
    void joinGroupByUrl_FAIL_ALREADY_JOIONED() {
        // given
        User user = User.builder().userEmail("test@abc.com").build();

        given(redisClient.get(any(), any()))
            .willReturn(invite);

        given(userRepository.findByUserEmail(any()))
            .willReturn(Optional.of(user));

        given(joinGroupRepository.existsByUser_IdAndGroup_Id(any(), any()))
            .willReturn(true);

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> groupJoinService.joinGroupByUrl(url));

        // then
        assertEquals(exception.getErrorCode(), ALREADY_JOINED_USER);
    }

    @Test
    @DisplayName("초대링크 그룹가입 실패_해당 그룹 없음")
    void joinGroupByUrl_FAIL_NOT_FOUND_GROUP() {
        // given
        User user = User.builder().userEmail("test@abc.com").build();

        given(redisClient.get(any(), any()))
            .willReturn(invite);

        given(userRepository.findByUserEmail(any()))
            .willReturn(Optional.of(user));

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> groupJoinService.joinGroupByUrl(url));

        // then
        assertEquals(exception.getErrorCode(), NOT_FOUND_GROUP);
    }
}