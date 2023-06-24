package com.study.api.group.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.study.api.client.RedisClient;
import com.study.api.exception.CustomException;
import com.study.api.exception.ErrorCode;
import com.study.api.group.dto.InviteDto;
import com.study.api.group.dto.InviteForm;
import com.study.api.group.dto.InviteForm.Exist;
import com.study.api.group.dto.InviteForm.New;
import com.study.api.group.dto.redis.Invite;
import com.study.domain.model.JoinGroup;
import com.study.domain.model.User;
import com.study.domain.repository.JoinGroupRepository;
import com.study.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupInviteServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JoinGroupRepository joinGroupRepository;
    @Mock
    private RedisClient redisClient;
    @InjectMocks
    private GroupInviteService groupInviteService;

    public final User manager = User.builder()
        .userEmail("manager@abc.com")
        .userName("manager")
        .build();
    public final InviteForm.New newForm = New.builder()
        .userEmail("new@abc.com")
        .userName("newUser")
        .phone("01012341234")
        .build();

    public final InviteForm.Exist existForm = Exist.builder().userEmail("exist@abc.com").build();

    @Test
    @DisplayName("신규회원 초대링크 생성 성공")
    void inviteNewUser_SUCCESS() {
        // given
        JoinGroup managerJoinGroup = JoinGroup.builder().isManager(true).build();

        given(userRepository.findByUserEmail(any()))
            .willReturn(Optional.of(manager));

        given(joinGroupRepository.findByUser_IdAndGroup_Id(any(), any()))
            .willReturn(Optional.of(managerJoinGroup));

        given(userRepository.existsByUserEmail(any()))
            .willReturn(false);

        // when
        InviteDto response = groupInviteService.inviteNewUser(1L, newForm, manager.getUserEmail());
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Invite> inviteCaptor = ArgumentCaptor.forClass(Invite.class);

        // then
        verify(redisClient, times(1)).put(urlCaptor.capture(), inviteCaptor.capture());
        Assertions.assertEquals(urlCaptor.getValue(), "invite:" + response.getUrl());
        Assertions.assertEquals(inviteCaptor.getValue().getUserEmail(), newForm.getUserEmail());
        Assertions.assertEquals(response.getReceiver(), newForm.getUserEmail());
        Assertions.assertEquals(response.getSender(), manager.getUserEmail());
    }

    @Test
    @DisplayName("신규회원 초대링크 생성 실패_매니저 X")
    void inviteNewUser_FAIL_NOT_MANAGER() {
        // given
        JoinGroup managerJoinGroup = JoinGroup.builder().isManager(false).build();
        given(userRepository.findByUserEmail(any()))
            .willReturn(Optional.of(manager));

        given(joinGroupRepository.findByUser_IdAndGroup_Id(any(), any()))
            .willReturn(Optional.of(managerJoinGroup));

        // when
        CustomException exception = Assertions.assertThrows(CustomException.class,
            () -> groupInviteService.inviteNewUser(1L, newForm, manager.getUserEmail()));

        // then
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.NOT_HAVE_MANAGER_AUTHORITY);
    }

    @Test
    @DisplayName("신규회원 초대링크 생성 실패_이메일 중복")
    void inviteNewUser_FAIL_INCORRECT_EMAIL() {
        // given
        JoinGroup managerJoinGroup = JoinGroup.builder().isManager(true).build();

        given(userRepository.findByUserEmail(any()))
            .willReturn(Optional.of(manager));

        given(joinGroupRepository.findByUser_IdAndGroup_Id(any(), any()))
            .willReturn(Optional.of(managerJoinGroup));

        given(userRepository.existsByUserEmail(any()))
            .willReturn(true);

        // when
        CustomException exception = Assertions.assertThrows(CustomException.class,
            () -> groupInviteService.inviteNewUser(1L, newForm, manager.getUserEmail()));

        // then
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.USER_EMAIL_ALREADY_EXIST);
    }

    @Test
    @DisplayName("기존회원 초대링크 생성 성공")
    void inviteExistUser_SUCCESS() {
        // given
        JoinGroup managerJoinGroup = JoinGroup.builder().isManager(true).build();
        User user = User.builder().userEmail(existForm.getUserEmail()).build();

        given(userRepository.findByUserEmail(any()))
            .willReturn(Optional.of(manager));

        given(joinGroupRepository.findByUser_IdAndGroup_Id(any(), any()))
            .willReturn(Optional.of(managerJoinGroup));

        given(userRepository.findByUserEmail(existForm.getUserEmail()))
            .willReturn(Optional.of(user));

        given(joinGroupRepository.existsByUser_IdAndGroup_Id(any(), any()))
            .willReturn(false);

        // when
        InviteDto response = groupInviteService.inviteExistUser(1L, existForm,
            manager.getUserEmail());
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Invite> inviteCaptor = ArgumentCaptor.forClass(Invite.class);

        // then
        verify(redisClient, times(1)).put(urlCaptor.capture(), inviteCaptor.capture());
        Assertions.assertEquals(urlCaptor.getValue(), "invite:" + response.getUrl());
        Assertions.assertEquals(inviteCaptor.getValue().getUserEmail(), existForm.getUserEmail());
        Assertions.assertEquals(response.getReceiver(), existForm.getUserEmail());
        Assertions.assertEquals(response.getSender(), manager.getUserEmail());
    }

    @Test
    @DisplayName("기존회원 초대링크 생성 실패_매니저 X")
    void inviteExistUser_FAIL_NOT_MANAGER() {
        // given
        JoinGroup managerJoinGroup = JoinGroup.builder().isManager(false).build();

        given(userRepository.findByUserEmail(any()))
            .willReturn(Optional.of(manager));

        given(joinGroupRepository.findByUser_IdAndGroup_Id(any(), any()))
            .willReturn(Optional.of(managerJoinGroup));

        // when
        CustomException exception = Assertions.assertThrows(CustomException.class,
            () -> groupInviteService.inviteExistUser(1L, existForm, manager.getUserEmail()));

        // then
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.NOT_HAVE_MANAGER_AUTHORITY);
    }

    @Test
    @DisplayName("기존회원 초대링크 생성 실패_이미 그룹에 가입된 유저")
    void inviteExistUser_FAIL_ALREADY_JOINED() {
        // given
        JoinGroup managerJoinGroup = JoinGroup.builder().isManager(true).build();

        given(userRepository.findByUserEmail(any()))
            .willReturn(Optional.of(manager));

        given(joinGroupRepository.findByUser_IdAndGroup_Id(any(), any()))
            .willReturn(Optional.of(managerJoinGroup));

        given(joinGroupRepository.existsByUser_IdAndGroup_Id(any(), any()))
            .willReturn(true);

        // when
        CustomException exception = Assertions.assertThrows(CustomException.class,
            () -> groupInviteService.inviteExistUser(1L, existForm, manager.getUserEmail()));

        // then
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.ALREADY_JOINED_USER);
    }

}