package com.study.api.group.controller;

import com.study.api.group.dto.GroupDto;
import com.study.api.group.dto.GroupDto.Response;
import com.study.api.group.service.GroupService;
import java.security.Principal;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<Response> makeGroup(@Valid @RequestBody GroupDto.Request request,
        Principal principal) {
        GroupDto.Response response = groupService.add(request, principal.getName());
        return ResponseEntity.ok(response);
    }
}
