package com.study.api.group.service;

import static com.study.api.exception.ErrorCode.NOT_FOUND_GROUP;
import static com.study.api.exception.ErrorCode.NOT_FOUND_USER;
import static com.study.api.exception.ErrorCode.NOT_HAVE_MANAGER_AUTHORITY;
import static com.study.api.exception.ErrorCode.USER_EMAIL_ALREADY_EXIST;
import static com.study.api.exception.ErrorCode.USER_NOT_JOINED_THIS_GROUP;

import com.study.api.exception.CustomException;
import com.study.api.group.dto.InviteDto;
import com.study.api.group.dto.InviteForm;
import com.study.domain.model.Group;
import com.study.domain.model.JoinGroup;
import com.study.domain.model.User;
import com.study.domain.repository.GroupRepository;
import com.study.domain.repository.JoinGroupRepository;
import com.study.domain.repository.UserRepository;
import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupInviteService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final JoinGroupRepository joinGroupRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${invite.url.expiration}")
    private Long urlExpiration;

    @Value("${invite:new:init-password}")
    private String initPassword;

    @Transactional
    public InviteDto inviteNewUser(Long groupId, InviteForm.New form, Principal principal) {
        this.checkManager(groupId, principal.getName());

        if (userRepository.existsByUserEmail(form.getUserEmail())) {
            throw new CustomException(USER_EMAIL_ALREADY_EXIST);
        }

        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));

        // 회원 생성 (임시)
        User user = userRepository.save(
            User.builder()
                .userName(form.getUserName())
                .userEmail(form.getUserEmail())
                .phone(form.getPhone())
                .password(initPassword)
                .isActive(false)
                .build());

        // 조인그룹 추가 (임시)
        JoinGroup joinGroup = joinGroupRepository.save(
            JoinGroup.builder()
                .isManager(false)
                .isActive(false)
                .build());

        user.add(joinGroup);
        group.add(joinGroup);

        // 초대 링크 생성
        return InviteDto.builder()
            .url(this.createLink(groupId))
            .sender(principal.getName())
            .receiver(form.getUserEmail())
            .build();

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

    private String createLink(Long joinGroupId) {

        String url = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue()
            .set("INVITE:" + joinGroupId, url, urlExpiration, TimeUnit.MILLISECONDS);
        return url;

    }


}
