package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.web.servlet.function.ServerResponse;

@AllArgsConstructor
@Getter
public class PgrstEx extends RuntimeException {
    private final transient ExCodeStatus codeStatus;
    private final transient ExInfo info;
    private final transient Object extInfo;

    public ServerResponse toResponse() {
        return ServerResponse.status(codeStatus.status())
                .body(codeStatus.toExRes(info, extInfo));
    }
}
