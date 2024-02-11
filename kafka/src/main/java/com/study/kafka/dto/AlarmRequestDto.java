package com.study.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlarmRequestDto {

    private String type;
    private String to;
    private String from;
    private String subject;
    private String content;

}
