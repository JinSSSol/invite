package com.study.api.group.service;

import static com.study.api.exception.ErrorCode.ALREADY_JOINED_USER;
import static com.study.api.exception.ErrorCode.NOT_FOUND_USER;
import static com.study.api.exception.ErrorCode.NOT_HAVE_MANAGER_AUTHORITY;
import static com.study.api.exception.ErrorCode.USER_EMAIL_ALREADY_EXIST;
import static com.study.api.exception.ErrorCode.USER_NOT_JOINED_THIS_GROUP;

import com.study.api.client.RedisClient;
import com.study.api.exception.CustomException;
import com.study.api.group.dto.InviteDto;
import com.study.api.group.dto.InviteForm;
import com.study.api.group.dto.redis.Invite;
import com.study.domain.model.JoinGroup;
import com.study.domain.model.User;
import com.study.domain.repository.JoinGroupRepository;
import com.study.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupInviteService {
    private final UserRepository userRepository;
    private final JoinGroupRepository joinGroupRepository;
    private final RedisClient redisClient;

    @Value("${redis.key.invite-prefix}")
    private String invitePrefix;

    @Transactional
    public InviteDto inviteNewUser(Long groupId, InviteForm.New form, String managerEmail) {

        this.checkManager(groupId, managerEmail);

        // 이메일 중복 확인
        if (userRepository.existsByUserEmail(form.getUserEmail())) {
            throw new CustomException(USER_EMAIL_ALREADY_EXIST);
        }

        // 초대 링크 생성
        String url = this.createLink();

        // 임시 회원 생성
        Invite invite = Invite.from(form);
        invite.setSenderEmailAndGroupId(managerEmail, groupId);

        // 레디스 저장
        this.putRedis(url, invite);
        return new InviteDto(url, managerEmail, invite.getUserEmail());

    }

    @Transactional
    public InviteDto inviteExistUser(Long groupId, InviteForm.Exist form, String managerEmail) {

        this.checkManager(groupId, managerEmail);

        // 회원인지 확인
        User user = userRepository.findByUserEmail(form.getUserEmail())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 이미 그룹에 가입되어 있는지 확인
        if (joinGroupRepository.existsByUser_IdAndGroup_Id(user.getId(), groupId)) {
            throw new CustomException(ALREADY_JOINED_USER);
        }

        // 초대 링크 생성
        String url = this.createLink();

        // 임시 회원 생성
        Invite invite = Invite.from(user);
        invite.setSenderEmailAndGroupId(managerEmail, groupId);

        // 레디스 저장
        this.putRedis(url, invite);
        return new InviteDto(url, managerEmail, invite.getUserEmail());
    }

    private void checkManager(Long groupId, String userEmail) {
        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        JoinGroup joinGroup = joinGroupRepository.findByUser_IdAndGroup_Id(user.getId(), groupId)
            .orElseThrow(() -> new CustomException(USER_NOT_JOINED_THIS_GROUP));

        if (!joinGroup.getIsManager()) {
            throw new CustomException(NOT_HAVE_MANAGER_AUTHORITY);
        }
    }

    private String createLink() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void putRedis(String url, Invite invite) {
        redisClient.putInvite(invitePrefix + ":" + url, invite);
    }
}
