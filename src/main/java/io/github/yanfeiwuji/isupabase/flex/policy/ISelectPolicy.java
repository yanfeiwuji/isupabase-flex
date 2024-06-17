package io.github.yanfeiwuji.isupabase.flex.policy;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.flex.TableOneOperateConfig;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:38
 */
public interface ISelectPolicy<C extends AuthContext, T> extends IPolicy<C, T> {
    default QueryCondition using(C context) {
        return QueryCondition.createEmpty();
    }

    default List<QueryColumn> columns(C context) {
        return null;
    }

    @Override
    default TableOneOperateConfig<C, T> config() {
        return new TableOneOperateConfig<>(
                (context) -> EMPTY_CONDITION,
                (context, ls) -> {
                },
                this::columns,
                (context, ls) -> {
                }
        );
    }
}
