package io.github.yanfeiwuji.isupabase.request.flex.policy;

import com.mybatisflex.core.query.QueryColumn;
import io.github.yanfeiwuji.isupabase.request.flex.*;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:40
 */
public class InsertPolicyBase<T> extends PolicyBase<T> {
    public void checking(PgrstContext context, List<T> entities) {

    }

    public List<QueryColumn> columns(PgrstContext context) {
        return null;
    }

    public void before(PgrstContext context, OperateInfo<T> operateInfo) {

    }

    public void after(PgrstContext context, OperateInfo<T> operateInfo) {

    }

    @Override
    TableSetting<T> config() {
        return new TableSetting<>(
                context -> EMPTY_CONDITION,
                this::checking,
                this::columns,
                this::before,
                this::after
        );
    }

    ;
}
