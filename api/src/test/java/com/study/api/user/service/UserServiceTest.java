package com.study.api.user.service;

import static com.study.api.exception.ErrorCode.NOT_FOUND_USER;
import static com.study.api.exception.ErrorCode.NOT_MATCHED_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.study.api.exception.CustomException;
import com.study.api.exception.ErrorCode;
import com.study.api.security.JwtProvider;
import com.study.api.user.dto.SignInForm;
import com.study.api.user.dto.SignUpForm;
import com.study.api.user.dto.TokenDto;
import com.study.api.user.dto.UserDto;
import com.study.domain.model.User;
import com.study.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private UserService userService;

    private final SignUpForm signUpForm = SignUpForm.builder()
        .userEmail("test@abc.com")
        .userName("테스트")
        .password("1234")
        .build();
    private final SignInForm signInForm = SignInForm.builder()
        .userEmail("test@abc.com")
        .password("1234").build();

    @Test
    @DisplayName("회원가입 성공")
    void signUp_SUCCESS() {
        // given
        given(userRepository.existsByUserEmail("test@abc.com"))
            .willReturn(false);

        given(userRepository.save(any()))
            .willReturn(User.builder()
                .id(1L)
                .userEmail("test@abc.com")
                .userName("테스트")
                .phone("00012341234")
                .password("1234")
                .build()
            );

        // when
        UserDto user = userService.signUp(signUpForm);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // then
        verify(userRepository, times(1)).save(captor.capture());
        assertEquals("test@abc.com", captor.getValue().getUserEmail());
        assertTrue(captor.getValue().getIsActive());
        assertEquals("test@abc.com", user.getUserEmail());
        assertEquals("테스트", user.getUserName());
        assertEquals("00012341234", user.getPhone());
    }

    @Test
    @DisplayName("회원가입 실패_이메일 중복")
    void signUp_FAIL() {
        // given
        given(userRepository.existsByUserEmail("test@abc.com"))
            .willReturn(true);

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.signUp(signUpForm));

        // then
        assertEquals(ErrorCode.USER_EMAIL_ALREADY_EXIST, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 성공")
    void signIn_SUCCESS() {
        // given
        User user = User.builder()
            .userEmail("test@abc.com")
            .userName("테스트")
            .password("1234")
            .phone("00012341234")
            .build();
        given(userRepository.findByUserEmail("test@abc.com"))
            .willReturn(Optional.of(user));

        given(passwordEncoder.matches(any(), any()))
            .willReturn(true);

        given(jwtProvider.generateToken("test@abc.com", List.of("ROLE_USER")))
            .willReturn("token");

        // when
        TokenDto tokenDto = userService.signIn(signInForm);

        // then
        assertEquals(tokenDto.getToken(), "token");
    }

    @Test
    @DisplayName("로그인 실패_잘못된 ID")
    void signIn_FAIL_INCORRECT_ID() {
        // given
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.signIn(signInForm));

        // then
        assertEquals(exception.getErrorCode(), NOT_FOUND_USER);
    }

    @Test
    @DisplayName("로그인 실패_잘못된 패스워드")
    void signIn_FAIL_INCORRECT_PASSWORD() {
        // given

        User user = User.builder()
            .userEmail("test@abc.com")
            .userName("테스트")
            .password("1234")
            .phone("00012341234")
            .build();
        given(userRepository.findByUserEmail("test@abc.com"))
            .willReturn(Optional.of(user));

        given(passwordEncoder.matches(any(), any()))
            .willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.signIn(signInForm));

        // then
        assertEquals(exception.getErrorCode(), NOT_MATCHED_PASSWORD);
    }

}