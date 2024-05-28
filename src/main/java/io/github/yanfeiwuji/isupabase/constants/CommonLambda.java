package io.github.yanfeiwuji.isupabase.constants;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.request.select.QueryExec;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class CommonLambda {

    public void emptyQueryExecAssembly(QueryExec queryExec, List<String> values) {
        //  cache by not handler
    }

    public void emptyFilterAssembly(QueryCondition queryExec, String values) {
        //  cache by not handler
    }
}
