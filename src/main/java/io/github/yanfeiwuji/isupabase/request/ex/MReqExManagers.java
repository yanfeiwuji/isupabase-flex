package io.github.yanfeiwuji.isupabase.request.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * code
 * :
 * "PGRST118"
 * details
 * :
 * "'sys_user' and 'sys_role' do not form a many-to-one or one-to-one
 * relationship"
 * hint
 * :
 * null
 * message
 * :
 * "A related order on 'sys_role' is not possible"
 */
@AllArgsConstructor
@Getter
public enum MReqExManagers implements IExManagers {
    FAILED_TO_PARSE(
            new ExResTemp("PGRST100",
                    null,
                    null,
                    "\"failed to parse filter (%s)\"",
                    HttpStatus.BAD_REQUEST)),

    ORDER_NO_APPLY(
            new ExResTemp("PGRST118",
                    null,
                    null,
                    "A related order on '%s' is not possible",
                    HttpStatus.BAD_REQUEST));

    private ExResTemp exResTemp;
}
