package com.study.api.group.controller;

import com.study.api.group.dto.JoinGroupDto;
import com.study.api.group.service.GroupJoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups/join")
public class GroupJoinController {

    private final GroupJoinService groupJoinService;
    @PostMapping()
    public ResponseEntity<JoinGroupDto> joinGroup(@RequestParam String url) {
        JoinGroupDto response = groupJoinService.joinGroupByUrl(url);
        return ResponseEntity.ok(response);
    }
}
