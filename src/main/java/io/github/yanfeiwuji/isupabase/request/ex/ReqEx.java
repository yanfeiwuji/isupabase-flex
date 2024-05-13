package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.function.ServerResponse;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ReqEx extends RuntimeException {
    private ExResTemp exResTemp;
    private Object[] args;

    public ReqEx(ExResTemp exResTemp, Object... args) {
        super(exResTemp.toExRes(args).toString());
        this.exResTemp = exResTemp;
        this.args = args;
    }

    public ServerResponse toResponse() {
        log.info("args:{}", args);
        return ServerResponse.status(exResTemp.httpStatus())
                .body(exResTemp.toExRes(args));
    }

}
