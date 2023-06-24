package com.study.api.group.service;

import static com.study.api.exception.ErrorCode.ALREADY_JOINED_USER;
import static com.study.api.exception.ErrorCode.INVALID_INVITE_URL;
import static com.study.api.exception.ErrorCode.NOT_FOUND_GROUP;

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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupJoinService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final JoinGroupRepository joinGroupRepository;
    private final RedisClient redisClient;
    @Value("${redis.key.invite-prefix}")
    private String invitePrefix;

    @Value("${invite.newUser.init-password}")
    private String initPassword;

    public JoinGroupDto joinGroupByUrl(String url) {

        Invite invite = this.getRedisByUrl(url);

        User user = userRepository.findByUserEmail(invite.getUserEmail())
            .orElseGet(() -> userRepository.save(invite.toUser(initPassword)));

        JoinGroup joinGroup = this.joinGroup(user, invite.getGroupId());

        this.deleteRedis(url);
        return JoinGroupDto.from(joinGroup, user.getUserEmail());
    }

    @Transactional
    protected JoinGroup joinGroup(User user, Long groupId) {

        this.checkAlreadyJoined(user.getId(), groupId);

        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));

        JoinGroup joinGroup = joinGroupRepository.save(
            JoinGroup.builder()
                .user(user)
                .group(group)
                .isManager(false)
                .build());

        group.add(joinGroup);
        user.add(joinGroup);

        return joinGroup;
    }

    private void checkAlreadyJoined(Long userId, Long groupId) {
        if (joinGroupRepository.existsByUser_IdAndGroup_Id(userId, groupId)) {
            throw new CustomException(ALREADY_JOINED_USER);
        }
    }

    private Invite getRedisByUrl(String url) {
        try {
            return redisClient.get(invitePrefix + ":" + url, Invite.class);
        } catch (CustomException e) {
            throw new CustomException(INVALID_INVITE_URL);
        }
    }

    private void deleteRedis(String url) {
        redisClient.delete(invitePrefix + ":" + url);
    }

}
