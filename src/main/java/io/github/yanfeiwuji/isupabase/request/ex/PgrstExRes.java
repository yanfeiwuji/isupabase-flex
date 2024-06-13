package io.github.yanfeiwuji.isupabase.request.ex;

import com.fasterxml.jackson.annotation.JsonInclude;

public record PgrstExRes(String code, String details,
                         String hint, String message,
                         @JsonInclude(JsonInclude.Include.NON_NULL)
                    Object extInfo) {

}
