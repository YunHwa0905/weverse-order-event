package com.weverse.exception;

public class MemberNotFoundException extends BusinessException {

    public MemberNotFoundException(String memberId) {
        super("회원을 찾을 수 없습니다. memberId: " + memberId);
    }
}
