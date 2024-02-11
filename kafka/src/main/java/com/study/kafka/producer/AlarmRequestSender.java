package com.study.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.kafka.constraint.KafkaTopic;
import com.study.kafka.dto.AlarmRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlarmRequestSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendMessage(KafkaTopic topic, AlarmRequestDto alarmRequestDto) {
        try {
            kafkaTemplate.send(topic.name(), objectMapper.writeValueAsString(alarmRequestDto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to send kafka!");
        }
    }

}
