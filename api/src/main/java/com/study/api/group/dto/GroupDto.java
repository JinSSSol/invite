package com.study.api.group.dto;

import com.study.domain.model.JoinGroup;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class GroupDto {

    @Getter
    public static class Request {

        String groupName;

    }

    @AllArgsConstructor
    @Getter
    public static class Response {

        Long id;
        String groupName;
        List<JoinGroup> users;
    }
}
