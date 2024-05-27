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
                    HttpStatus.BAD_REQUEST)),
    COULD_NOT_FIND_REL(
            new ExResTemp("PGRST200",
                    "Searched relationship between '%s' and '%s' in database ,but no matches were found.",
                    null,
                    "Could not find a relationship between '%s' and '%s' in the schema cache",
                    HttpStatus.BAD_REQUEST)),
    NOT_EMBEDDED(
            new ExResTemp("PGRST108",
                    null,
                    "Verify that '%s' is included in the 'select' query",
                    "'%s' is not an embedded resource in this request",
                    HttpStatus.BAD_REQUEST))

    ;

    // {
    // "code": "",
    // "details": null,
    // "hint": "Verify that 'sys_usert_ext' is included in the 'select' query
    // parameter.",
    // "message": "'sys_usert_ext' is not an embedded resource in this request"
    // }
    private ExResTemp exResTemp;
}
