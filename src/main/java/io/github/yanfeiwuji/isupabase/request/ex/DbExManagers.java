package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

@AllArgsConstructor
@Getter
public enum DbExManagers {
    UNDEFINED_TABLE(
            new ExResTemp("42P01",
                    null,
                    null,
                    "relation \"%s\" does not exist",
                    HttpStatus.NOT_FOUND
            ));
    private ExResTemp exResTemp;

    public Supplier<ReqEx> supplierReqEx(Object... args) {
        return () -> new ReqEx(exResTemp, args);
    }




}
