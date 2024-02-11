package com.study.alarm.service;

import com.study.alarm.constants.AlarmType;
import com.study.alarm.dto.AlarmDto;
import com.study.kafka.constraint.KafkaTopic;
import com.study.kafka.producer.AlarmRequestSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRequestSender alarmRequestSender;

    public void sendAlarmMessage(AlarmDto alarm) {
        alarmRequestSender.sendMessage(getTopic(alarm.getType()), alarm.to());
    }

    private KafkaTopic getTopic(AlarmType alarmType) {
        if (alarmType == AlarmType.ANDROID) {
            return KafkaTopic.ALARM_ANDROID;
        }
        if (alarmType == AlarmType.APPLE) {
            return KafkaTopic.ALARM_APPLE;
        }
        if (alarmType == AlarmType.EMAIL) {
            return KafkaTopic.ALARM_EMAIL;
        }

        throw new RuntimeException("해당하는 카프카 토픽이 존재하지 않습니다.");
    }

}
