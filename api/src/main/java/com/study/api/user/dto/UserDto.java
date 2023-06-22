package com.study.api.user.dto;

import com.study.domain.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {

    private String userName;
    private String userEmail;
    private String phone;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
            .userName(user.getUserName())
            .userEmail(user.getUserEmail())
            .phone(user.getPhone())
            .build();
    }
}
