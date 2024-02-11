package com.study.alarm.dto;

import com.study.alarm.constants.AlarmType;
import com.study.kafka.dto.AlarmRequestDto;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AlarmDto {

    private AlarmType type;
    private String to;
    private String from;
    private String subject;
    private String content;

    public AlarmRequestDto to() {
        return AlarmRequestDto.builder()
            .type(this.type.name())
            .to(this.to)
            .from(this.from)
            .subject(this.subject)
            .content(this.content)
            .build();
    }
}
