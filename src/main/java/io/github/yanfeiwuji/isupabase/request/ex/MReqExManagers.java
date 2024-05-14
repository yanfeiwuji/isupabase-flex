package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MReqExManagers implements IExManagers {
    FAILED_TO_PARSE(
            new ExResTemp("PGRST100",
                    null,
                    null,
                    "\"failed to parse filter (%s)\"",
                    HttpStatus.BAD_REQUEST));;
    private ExResTemp exResTemp;
}
