package com.study.kafka.constraint;

public enum KafkaTopic {
    ALARM_REQUEST("alarm_request"),
    ALARM_ANDROID("alarm_android"),
    ALARM_APPLE("alarm_apple"),
    ALARM_EMAIL("alarm_email");
    final String topic;

    KafkaTopic(String topicName) {
        this.topic = topicName;
    }
}
