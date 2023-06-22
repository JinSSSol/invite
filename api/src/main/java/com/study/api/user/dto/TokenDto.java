package com.study.api.user.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenDto {
    String token;
    Date expiredTime;
}
