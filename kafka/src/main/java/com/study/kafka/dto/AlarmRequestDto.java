package com.study.kafka.dto;

import lombok.Builder;

@Builder
public class AlarmRequestDto {

    private String type;
    private String to;
    private String from;
    private String subject;
    private String content;

}
