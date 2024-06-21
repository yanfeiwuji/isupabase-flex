package io.github.yanfeiwuji.isupabase.request.flex.policy;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.request.flex.*;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:42
 */
public abstract class UpdatePolicyBase<T> extends PolicyBase<T> {
    public QueryCondition using(PgrstContext context) {
        return QueryCondition.createEmpty();
    }

    public void checking(PgrstContext context, List<T> entities) {

    }

    public List<QueryColumn> columns(PgrstContext context) {
        return null;
    }


    @Override
    TableSetting<T> config() {
        return new TableSetting<>(
                this::using,
                this::checking,
                this::columns
        );
    }
}
