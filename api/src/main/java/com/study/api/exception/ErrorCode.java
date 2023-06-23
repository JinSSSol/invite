package com.study.api.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    NOT_FOUND_USER(BAD_REQUEST, "사용자를 찾을 수 없습니다."),
    USER_EMAIL_ALREADY_EXIST(BAD_REQUEST, "이미 존재하는 이메일 입니다."),
    NOT_MATCHED_PASSWORD(BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    // jwt
    INVALID_TOKEN(UNAUTHORIZED, "토큰이 올바르지 않습니다."),
    UNKNOWN_ERROR(UNAUTHORIZED, "인증 관련 알수없는 에러입니다."),
    EXPIRED_TOKEN(UNAUTHORIZED, "토큰이 만료되었습니다."),
    EMPTY_TOKEN_ERROR(UNAUTHORIZED, "토큰이 비어있습니다."),
    FAILED_VERIFY_SIGNATURE(UNAUTHORIZED, "시그니처 검증에 실패한 토큰입니다."),
    INVALID_TOKEN_IN_HEADER(UNAUTHORIZED, "헤더에 유효한 토큰이 존재하지 않습니다."),

    // 그룹 초대
    NOT_FOUND_GROUP(BAD_REQUEST, "그룹을 찾을 수 없습니다."),
    USER_NOT_JOINED_THIS_GROUP(BAD_REQUEST, "해당 그룹 회원이 아닙니다."),
    NOT_HAVE_MANAGER_AUTHORITY(BAD_REQUEST, "매니저 권한이 없습니다.");
    private final HttpStatus httpStatus;
    private final String message;


}
