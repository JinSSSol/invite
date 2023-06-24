package com.study.domain.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.study.domain.model.Group;
import com.study.domain.model.JoinGroup;
import com.study.domain.model.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JoinGroupRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private JoinGroupRepository joinGroupRepository;

    @Test
    @DisplayName("그룹테이블 ID로 조인그룹 조회_성공")
    void findByGroup_Id_SUCCESS() {
        // given
        User user = User.builder()
            .userEmail("test@abc.com")
            .userName("테스트").build();
        userRepository.save(user);

        Group group = Group.builder()
            .groupName("testGroup").build();
        groupRepository.save(group);

        JoinGroup joinGroup = joinGroupRepository.save(JoinGroup.builder()
            .user(user)
            .group(group)
            .build());

        // when
        Optional<JoinGroup> findJoinGroup = joinGroupRepository.findByGroup_Id(1L);

        // then
        assertTrue(findJoinGroup.isPresent());
        assertEquals(joinGroup, findJoinGroup.get());
        assertEquals(findJoinGroup.get().getGroup().getGroupName(), "testGroup");
        assertEquals(findJoinGroup.get().getUser().getUserEmail(), "test@abc.com");
    }


    @Test
    @DisplayName("그룹테이블 ID로 조인그룹 조회_실패")
    void findByGroup_Id_FAIL() {
        // given
        // when
        boolean result = joinGroupRepository.findByGroup_Id(1L).isPresent();

        // then
        assertFalse(result);

    }

}