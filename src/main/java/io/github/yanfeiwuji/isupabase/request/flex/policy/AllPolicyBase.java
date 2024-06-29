package io.github.yanfeiwuji.isupabase.request.flex.policy;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.request.flex.*;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:37
 */

public interface AllPolicyBase<T> extends PolicyBase<T> {
    default QueryCondition using(PgrstContext context) {
        return QueryCondition.createEmpty();
    }

    default void checking(PgrstContext context, List<T> entities) {

    }

    default List<QueryColumn> columns(PgrstContext context) {
        return null;
    }

    default TableSetting<T> config() {
        return new TableSetting<>(
                this::using,
                this::checking,
                this::columns
        );
    }
}
