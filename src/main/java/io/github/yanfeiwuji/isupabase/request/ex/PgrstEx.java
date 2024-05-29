package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class PgrstEx extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;
    private ExCodeStatus codeStatus;
    private ExInfo info;

    public ServerResponse toResponse() {
        return ServerResponse.status(codeStatus.status())
                .body(codeStatus.toExRes(info));
    }
}
