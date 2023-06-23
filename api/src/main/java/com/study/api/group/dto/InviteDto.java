package com.study.api.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class InviteDto {

    String url;
    String sender;
    String receiver;

}
