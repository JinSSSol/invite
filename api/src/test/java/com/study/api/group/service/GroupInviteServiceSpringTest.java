package com.study.api.group.service;

import static com.study.api.exception.ErrorCode.ALREADY_JOINED_USER;
import static com.study.api.exception.ErrorCode.NOT_HAVE_MANAGER_AUTHORITY;
import static com.study.api.exception.ErrorCode.USER_EMAIL_ALREADY_EXIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.study.api.exception.CustomException;
import com.study.api.group.dto.InviteDto;
import com.study.api.group.dto.InviteForm;
import com.study.api.group.dto.InviteForm.Exist;
import com.study.domain.model.Group;
import com.study.domain.model.JoinGroup;
import com.study.domain.model.User;
import com.study.domain.repository.GroupRepository;
import com.study.domain.repository.JoinGroupRepository;
import com.study.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestInstance(Lifecycle.PER_CLASS)
class GroupInviteServiceSpringTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private JoinGroupRepository joinGroupRepository;

    @Autowired
    private GroupInviteService groupInviteService;

    private Group group;

    @BeforeAll
    void setUp() {
        // 회원(매니저) 생성
        User manager = userRepository.save(User.builder()
            .userEmail("manager@abc.com")
            .userName("manager")
            .phone("00012341234")
            .password("12341234")
            .build());

        // 그룹 생성
        group = groupRepository.save(Group.builder().groupName("testGroup").build());
        JoinGroup joinGroup = joinGroupRepository.save(JoinGroup.builder()
            .user(manager)
            .group(group)
            .isManager(true)
            .build());
        manager.add(joinGroup);
        group.add(joinGroup);

    }

    @Test
    @DisplayName("회원초대 성공_신규 회원")
    void inviteNewUser_SUCCESS() {
        // given
        InviteForm.New form = InviteForm.New.builder()
            .userEmail("new@abc.com")
            .userName("newUser")
            .phone("00012341234")
            .build();

        Long groupId = 1L;
        String managerEmail = "manager@abc.com";

        // when
        InviteDto response = groupInviteService.inviteNewUser(groupId, form, managerEmail);

        // then
        assertEquals(response.getReceiver(), form.getUserEmail());
        assertEquals(response.getSender(), managerEmail);
        assertNotNull(response.getUrlCode());
    }

    @Test
    @DisplayName("회원초대 실패_신규회원_이메일 중복")
    void inviteNewUser_FAIL_EXIST_EMAIL() {
        // given
        User existUser = userRepository.save(User.builder()
            .userEmail("new@abc.com")
            .userName("newUser")
            .build());

        InviteForm.New form = InviteForm.New.builder()
            .userEmail(existUser.getUserEmail())
            .userName("newUser")
            .phone("00012341234")
            .build();

        Long groupId = 1L;
        String managerEmail = "manager@abc.com";

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> groupInviteService.inviteNewUser(groupId, form, managerEmail));

        // then
        assertEquals(exception.getErrorCode(), USER_EMAIL_ALREADY_EXIST);
    }

    @Test
    @DisplayName("회원초대 실패_신규회원_매니저 권한 없음")
    void inviteNewUser_FAIL_NOT_MANAGER() {
        // given
        User notMangerUser = userRepository.save(User.builder()
            .userEmail("user@abc.com")
            .userName("User")
            .phone("00012341234")
            .password("12341234")
            .build());

        JoinGroup joinGroup = joinGroupRepository.save(JoinGroup.builder()
            .user(notMangerUser)
            .group(group)
            .isManager(false)
            .build());
        notMangerUser.add(joinGroup);
        group.add(joinGroup);

        InviteForm.New form = InviteForm.New.builder()
            .userEmail("new@abc.com")
            .userName("newUser")
            .phone("00012341234")
            .build();

        Long groupId = 1L;

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> groupInviteService.inviteNewUser(groupId, form, notMangerUser.getUserEmail()));

        // then
        assertEquals(exception.getErrorCode(), NOT_HAVE_MANAGER_AUTHORITY);
    }

    @Test
    @DisplayName("회원초대 성공_기존 회원")
    void inviteExistUser_SUCCESS() {
        // given
        User existUser = userRepository.save(User.builder()
            .userEmail("exist@abc.com")
            .userName("existUser")
            .phone("00012341234")
            .password("12341234")
            .build());

        InviteForm.Exist form = new Exist("exist@abc.com");
        Long groupId = 1L;
        String managerEmail = "manager@abc.com";

        // when
        InviteDto response = groupInviteService.inviteExistUser(groupId, form, managerEmail);

        // then
        assertEquals(response.getReceiver(), existUser.getUserEmail());
        assertEquals(response.getSender(), managerEmail);
        assertNotNull(response.getUrlCode());
    }

    @Test
    @DisplayName("회원초대 실패_기존 회원_이미 가입된 회원")
    void inviteExistUser_FAIL() {
        // given
        User existUser = userRepository.save(User.builder()
            .userEmail("exist@abc.com")
            .userName("existUser")
            .phone("00012341234")
            .password("12341234")
            .build());

        JoinGroup joinGroup = joinGroupRepository.save(JoinGroup.builder()
            .user(existUser)
            .group(group)
            .isManager(false)
            .build());
        existUser.add(joinGroup);
        group.add(joinGroup);

        InviteForm.Exist form = new Exist("exist@abc.com");
        Long groupId = 1L;
        String managerEmail = "manager@abc.com";

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> groupInviteService.inviteExistUser(groupId, form, managerEmail));

        // then
        assertEquals(exception.getErrorCode(), ALREADY_JOINED_USER);
    }

}