package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MDbExManagers implements IExManagers {
    UNDEFINED_TABLE(
            new ExResTemp("42P01",
                    null,
                    null,
                    "relation \"%s\" does not exist",
                    HttpStatus.NOT_FOUND)),
    COLUMN_NOT_FOUND(
            new ExResTemp("42P01",
                    null,
                    null,
                    "column \"%s\" does not exist",
                    HttpStatus.NOT_FOUND)),
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
                    HttpStatus.BAD_REQUEST)
    );
    private final ExResTemp exResTemp;

}
