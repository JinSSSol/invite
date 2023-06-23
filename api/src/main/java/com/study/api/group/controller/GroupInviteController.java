package com.study.api.group.controller;

import com.study.api.group.dto.InviteDto;
import com.study.api.group.dto.InviteForm;
import com.study.api.group.service.GroupInviteService;
import java.security.Principal;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups/invite")
public class GroupInviteController {

    private final GroupInviteService groupInviteService;

    @PostMapping("/new/{groupId}")
    public ResponseEntity<InviteDto> createInviteLink(@PathVariable Long groupId,
        @Valid @RequestBody InviteForm.New form, Principal principal) {
        InviteDto response = groupInviteService.inviteNewUser(groupId, form, principal);
        return ResponseEntity.ok(response);
    }

}
