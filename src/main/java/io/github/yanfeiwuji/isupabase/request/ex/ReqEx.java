package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

import org.springframework.web.servlet.function.ServerResponse;

@EqualsAndHashCode(callSuper = true)
@Slf4j

public class ReqEx extends RuntimeException implements Serializable {
    private final ExResTemp exResTemp;
    private final ExResArgs exResArgs;

    public ReqEx(ExResTemp exResTemp, ExResArgs exResArgs) {
        super(exResTemp.toExRes(exResArgs).toString());
        this.exResTemp = exResTemp;
        this.exResArgs = exResArgs;
    }

    public ServerResponse toResponse() {
        log.info("args:{}", exResArgs);
        return ServerResponse.status(exResTemp.httpStatus())
                .body(exResTemp.toExRes(exResArgs));
    }

}
