package io.github.yanfeiwuji.isupabase.auth.ex;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 10:47
 */
@RestControllerAdvice
public class AuthExAdvice {
    @ExceptionHandler(value = AuthEx.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthExRes authEx(AuthEx authEx) {
        return authEx.getAuthExRes();
    }

    @ExceptionHandler(value = AuthCmEx.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthCmExRes authCmEx(AuthCmEx authCmEx) {
        return authCmEx.getAuthCmExRes();
    }

}
