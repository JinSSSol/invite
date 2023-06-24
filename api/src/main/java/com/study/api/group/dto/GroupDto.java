package com.study.api.group.dto;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class GroupDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Request {

        @Size(max = 20, message = "그룹명은 20자 이하로 가능합니다.")
        private String groupName;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {

        private Long id;
        private String groupName;
    }
}
