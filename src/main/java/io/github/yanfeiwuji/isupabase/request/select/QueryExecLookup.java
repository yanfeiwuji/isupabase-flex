package io.github.yanfeiwuji.isupabase.request.select;

import java.util.Map;

public record QueryExecLookup(QueryExec queryExec,
                              Map<String, QueryExec> indexed
) {

}
