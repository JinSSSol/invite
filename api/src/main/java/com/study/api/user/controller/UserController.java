package com.study.api.user.controller;

import com.study.api.user.dto.SignInForm;
import com.study.api.user.dto.SignUpForm;
import com.study.api.user.dto.TokenDto;
import com.study.api.user.dto.UserDto;
import com.study.api.user.service.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody SignUpForm form) {
        return ResponseEntity.ok(userService.signUp(form));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<TokenDto> signIn(@Valid @RequestBody SignInForm form) {
        return ResponseEntity.ok(userService.signIn(form));
    }

}
