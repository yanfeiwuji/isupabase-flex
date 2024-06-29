package io.github.yanfeiwuji.isupabase.request.flex.policy;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.request.flex.*;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:40
 */
public interface InsertPolicyBase<T> extends PolicyBase<T> {
    default void checking(PgrstContext context, List<T> entities) {

    }

    default List<QueryColumn> columns(PgrstContext context) {
        return null;
    }


    @Override
    default TableSetting<T> config() {
        return new TableSetting<>(
                context -> QueryCondition.createEmpty(),
                this::checking,
                this::columns
        );
    }

    ;
}
