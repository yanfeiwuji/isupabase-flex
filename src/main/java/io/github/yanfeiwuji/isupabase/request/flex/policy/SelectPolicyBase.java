package io.github.yanfeiwuji.isupabase.request.flex.policy;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.request.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.request.flex.PgrstContext;
import io.github.yanfeiwuji.isupabase.request.flex.TableOneOperateConfig;
import io.github.yanfeiwuji.isupabase.request.flex.TableSetting;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:38
 */
public abstract class SelectPolicyBase<T> extends PolicyBase<T> {
    public QueryCondition using(PgrstContext context) {
        return QueryCondition.createEmpty();
    }

    public List<QueryColumn> columns(PgrstContext context) {
        return null;
    }

    public TableSetting<T> config() {
        return new TableSetting<>(
                this::using,
                (context, info) -> {
                },
                this::columns,
                (context, info) -> {
                },
                (context, info) -> {

                }
        );
    }
}
