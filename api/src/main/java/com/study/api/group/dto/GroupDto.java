package com.study.api.group.dto;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class GroupDto {

    @AllArgsConstructor
    @Getter
    public static class Request {

        @Size(max = 20, message = "그룹명은 20자 이하로 가능합니다.")
        String groupName;

    }

    @AllArgsConstructor
    @Getter
    public static class Response {

        Long id;
        String groupName;
    }
}
