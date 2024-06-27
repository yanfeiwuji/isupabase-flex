package io.github.yanfeiwuji.isupabase.request.flex.policy;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.request.flex.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:37
 */

public abstract class AllPolicyBase<T> extends PolicyBase<T> {
    public QueryCondition using(PgrstContext context) {
        return QueryCondition.createEmpty();
    }

    public void checking(PgrstContext context, List<T> entities) {

    }

    public List<QueryColumn> columns(PgrstContext context) {
        return null;
    }

    public QueryCondition and(QueryCondition... queryConditions) {
        return Arrays.stream(queryConditions).reduce(QueryCondition.createEmpty(), QueryCondition::and);
    }

    public QueryCondition or(QueryCondition... queryConditions) {
        return Arrays.stream(queryConditions).reduce(QueryCondition.createEmpty(), QueryCondition::or);
    }


    TableSetting<T> config() {
        return new TableSetting<>(
                this::using,
                this::checking,
                this::columns
        );
    }
}
