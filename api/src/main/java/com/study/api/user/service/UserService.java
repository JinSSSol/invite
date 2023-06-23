package com.study.api.user.service;

import static com.study.api.exception.ErrorCode.NOT_FOUND_USER;
import static com.study.api.exception.ErrorCode.NOT_MATCHED_PASSWORD;
import static com.study.api.exception.ErrorCode.USER_EMAIL_ALREADY_EXIST;

import com.study.api.exception.CustomException;
import com.study.api.security.JwtProvider;
import com.study.api.user.dto.SignInForm;
import com.study.api.user.dto.SignUpForm;
import com.study.api.user.dto.TokenDto;
import com.study.api.user.dto.UserDto;
import com.study.domain.model.User;
import com.study.domain.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserDto signUp(SignUpForm form) {
        if (userRepository.existsByUserEmail(form.getUserEmail())) {
            throw new CustomException(USER_EMAIL_ALREADY_EXIST);
        }

        return UserDto.fromEntity(
            userRepository.save(User.builder()
                .userEmail(form.getUserEmail())
                .userName(form.getUserName())
                .phone(form.getPhone())
                .password(passwordEncoder.encode(form.getPassword()))
                .isActive(true)
                .build()));
    }

    public TokenDto signIn(SignInForm form) {
        User user = this.getUserByEmail(form.getUserEmail());

        if (!this.passwordEncoder.matches(form.getPassword(), user.getPassword())) {
            throw new CustomException(NOT_MATCHED_PASSWORD);
        }

        String token = jwtProvider.generateToken(user.getUserEmail(), List.of("ROLE_USER"));
        return new TokenDto(token, jwtProvider.getTokenExpireTime(token));
    }

    private User getUserByEmail(String userEmail) {
        return userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }
}
