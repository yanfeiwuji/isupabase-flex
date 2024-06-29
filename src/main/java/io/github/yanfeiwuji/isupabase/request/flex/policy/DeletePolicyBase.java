package io.github.yanfeiwuji.isupabase.request.flex.policy;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.request.flex.*;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:42
 */
public interface DeletePolicyBase<T> extends PolicyBase<T> {
    default QueryCondition using(PgrstContext context) {
        return QueryCondition.createEmpty();
    }


    @Override
    default TableSetting<T> config() {
        return new TableSetting<>(
                this::using,
                (context, info) -> {
                },
                (context) -> null
        );
    }
}
