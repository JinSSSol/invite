package com.study.alarm.service;

import com.study.alarm.constants.AlarmType;
import com.study.alarm.dto.AlarmDto;
import com.study.kafka.producer.AlarmRequestSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AlarmServiceSpringTest {

    @Autowired
    private AlarmRequestSender alarmRequestSender;
    @Autowired
    private AlarmService alarmService;

    @Test
    @DisplayName("알림 메시지 카프카 전송 성공")
    void sendAlarmMessage() {
        // given
        AlarmDto alarmDto = AlarmDto.builder()
            .type(AlarmType.APPLE)
            .to("test_to")
            .from("test_from")
            .subject("test_sub")
            .content("스프링 테스트입니다.")
            .build();

        // when
        // then
        alarmService.sendAlarmMessage(alarmDto);

    }
}