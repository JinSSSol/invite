package com.study.api.group.dto;

import com.study.domain.model.JoinGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class JoinGroupDto {

    String userEmail;
    Long groupId;
    String groupName;
    Boolean isManager;

    public static JoinGroupDto from(JoinGroup joinGroup, String userEmail) {
        return JoinGroupDto.builder()
            .userEmail(userEmail)
            .groupId(joinGroup.getGroup().getId())
            .groupName(joinGroup.getGroup().getGroupName())
            .isManager(joinGroup.getIsManager())
            .build();
    }

}
