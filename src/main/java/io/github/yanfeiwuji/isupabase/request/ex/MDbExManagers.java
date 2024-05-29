package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MDbExManagers implements IExManagers {


    // use java type not db type
    INVALID_INPUT(
            new ExResTemp("22P02",
                    null,
                    null,
                    "invalid input syntax for type %s \"%s\"",
                    HttpStatus.BAD_REQUEST)),
    DATATYPE_MISMATCH(
            new ExResTemp(
                    "42804",
                    null,
                    null,
                    "argument of %S must be type %s, not type %s",
                    HttpStatus.BAD_REQUEST)),
    UNDEFIDEND_COLUMN(new ExResTemp(
            "42703",
            null,
            null,
            "column %s.%s does not exist",
            HttpStatus.BAD_REQUEST));

    private final ExResTemp exResTemp;

}
