package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DbEx extends RuntimeException {
    private ExResHttpStatus exResHttpStatus;

    public DbEx(ExResHttpStatus exResHttpStatus) {
        super(exResHttpStatus.toString());
        this.exResHttpStatus = exResHttpStatus;
    }
}
