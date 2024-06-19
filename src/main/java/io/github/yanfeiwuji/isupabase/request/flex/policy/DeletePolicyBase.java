package io.github.yanfeiwuji.isupabase.request.flex.policy;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.request.flex.*;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:42
 */
public class DeletePolicyBase<T> extends PolicyBase<T> {
    public QueryCondition using(PgrstContext context) {
        return QueryCondition.createEmpty();
    }


    public void before(PgrstContext context, OperateInfo<T> operateInfo) {

    }

    public void after(PgrstContext context, OperateInfo<T> operateInfo) {

    }

    @Override
    TableSetting<T> config() {
        return new TableSetting<>(
                this::using,
                (context, info) -> {
                },
                (context) -> null,
                this::before,
                this::after
        );
    }
}
