package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * handler
 * <a href="https://postgrest.org/en/v12/references/errors.html#postgrest-error-codes">...</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqEx extends RuntimeException {
    private ExResHttpStatus exResHttpStatus;

    public ReqEx(ExResHttpStatus exResHttpStatus) {
        super(exResHttpStatus.toString());
        this.exResHttpStatus = exResHttpStatus;
    }

}
