package com.study.alarm.constants;

public enum AlarmType {
    ANDROID("안드로이드"),
    APPLE("애플"),
    EMAIL("이메일");
    final String type;

    AlarmType(String type) {
        this.type = type;
    }
}
