package com.study.api.group.service;

import static com.study.api.exception.ErrorCode.ALREADY_JOINED_USER;
import static com.study.api.exception.ErrorCode.INVALID_INVITE_URL_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class GroupJoinServiceSpringTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private JoinGroupRepository joinGroupRepository;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private GroupJoinService groupJoinService;

    @Value("${redis.key.invite-prefix}")
    private String invitePrefix;

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

    @AfterAll
    void clear() {
        joinGroupRepository.deleteAll();
        userRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    @DisplayName("그룹가입 성공_신규회원")
    void joinGroupByUrl_SUCCESS_NEW() {
        // given
        String urlCode = "testUrl";
        Invite invite = Invite.builder()
            .groupId(1L)
            .userEmail("invite@abc.com")
            .userName("inviteUser")
            .userPhone("00012341234")
            .senderEmail("manager@abc.com")
            .build();

        redisClient.putInvite(invitePrefix + ":" + urlCode, invite);

        // when
        JoinGroupDto joinGroupDto = groupJoinService.joinGroupByUrl(urlCode);

        // then
        assertEquals(joinGroupDto.getGroupId(), group.getId());
        assertEquals(joinGroupDto.getUserEmail(), "invite@abc.com");
        assertFalse(joinGroupDto.getIsManager());

        User user = userRepository.findByUserEmail(joinGroupDto.getUserEmail())
            .orElseThrow(() -> new RuntimeException("FAILED_TEST"));
        assertEquals(user.getUserName(), invite.getUserName());

        JoinGroup joinGroup = joinGroupRepository.findByUser_IdAndGroup_Id(user.getId(),
                group.getId())
            .orElseThrow(() -> new RuntimeException("FAILED_TEST"));
        assertNotNull(joinGroup);

    }

    @Test
    @DisplayName("그룹가입 성공_기존회원")
    void joinGroupByUrl_SUCCESS_EXIST() {
        // given
        User inviteUser = userRepository.save(User.builder()
            .userEmail("invite@abc.com")
            .userName("inviteUser")
            .phone("00012341234")
            .password("12341234")
            .build());

        String urlCode = "testUrl";
        Invite invite = Invite.builder()
            .groupId(group.getId())
            .userEmail("invite@abc.com")
            .userName("inviteUser")
            .userPhone("00012341234")
            .senderEmail("manager@abc.com")
            .build();

        redisClient.putInvite(invitePrefix + ":" + urlCode, invite);

        // when
        JoinGroupDto joinGroupDto = groupJoinService.joinGroupByUrl(urlCode);

        // then
        assertEquals(joinGroupDto.getGroupId(), group.getId());
        assertEquals(joinGroupDto.getUserEmail(), "invite@abc.com");
        assertFalse(joinGroupDto.getIsManager());

        JoinGroup joinGroup = joinGroupRepository.findByUser_IdAndGroup_Id(inviteUser.getId(),
                group.getId())
            .orElseThrow(() -> new RuntimeException("FAILED_TEST"));
        assertNotNull(joinGroup);
    }

    @Test
    @DisplayName("그룹가입 실패_잘못된 URL")
    void joinGroupByUrl_FAIL_INVALID_URL() {
        // given
        String urlCode = "invalidUrl";

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> groupJoinService.joinGroupByUrl(urlCode));

        // then
        assertEquals(exception.getErrorCode(), INVALID_INVITE_URL_CODE);
    }

    @Test
    @DisplayName("그룹가입 실패_이미 가입된 유저")
    void joinGroupByUrl_FAIL_ALREADY_JOINED() {
        // given
        User joinedUser = userRepository.save(User.builder()
            .userEmail("joined@abc.com")
            .userName("joinedUser")
            .phone("00012341234")
            .password("12341234")
            .build());

        joinGroupRepository.save(JoinGroup.builder()
            .user(joinedUser)
            .group(group)
            .isManager(false)
            .build());

        String urlCode = "testUrl";
        Invite invite = Invite.builder()
            .groupId(group.getId())
            .userEmail("joined@abc.com")
            .userName("joinedUser")
            .userPhone("00012341234")
            .senderEmail("manager@abc.com")
            .build();

        redisClient.putInvite(invitePrefix + ":" + urlCode, invite);

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> groupJoinService.joinGroupByUrl(urlCode));

        // then
        assertEquals(exception.getErrorCode(), ALREADY_JOINED_USER);
    }
}