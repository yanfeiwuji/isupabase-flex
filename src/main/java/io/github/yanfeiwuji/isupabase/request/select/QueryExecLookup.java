package io.github.yanfeiwuji.isupabase.request.select;

import java.util.List;
import java.util.Map;

public record QueryExecLookup(QueryExec queryExec,
                              Map<String, QueryExec> indexed,
                              List<String> removeJsonPath,
                              List<ResultMapping> renameJsonPath
) {

}
