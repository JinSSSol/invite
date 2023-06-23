package com.study.api.group.service;

import static com.study.api.exception.ErrorCode.NOT_FOUND_USER;

import com.study.api.exception.CustomException;
import com.study.api.group.dto.GroupDto;
import com.study.domain.model.Group;
import com.study.domain.model.JoinGroup;
import com.study.domain.model.User;
import com.study.domain.repository.GroupRepository;
import com.study.domain.repository.JoinGroupRepository;
import com.study.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final JoinGroupRepository joinGroupRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupDto.Response add(GroupDto.Request request, String userEmail) {
        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        Group group = groupRepository.save(
            Group.builder().groupName(request.getGroupName()).build());

        JoinGroup joinGroup = joinGroupRepository.save(JoinGroup.builder()
            .group(group)
            .user(user)
            .isActive(true)
            .isManager(true)
            .build());

        group.getUsers().add(joinGroup);
        user.getGroups().add(joinGroup);

        return new GroupDto.Response(group.getId(), group.getGroupName());
    }


}
