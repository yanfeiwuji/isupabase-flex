package io.github.yanfeiwuji.isupabase.flex.policy;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.flex.OperateInfo;
import io.github.yanfeiwuji.isupabase.flex.TableOneOperateConfig;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:42
 */
public interface IUpdatePolicy<C extends AuthContext, T> extends IPolicy<C, T> {
    default QueryCondition using(C context) {
        return QueryCondition.createEmpty();
    }

    default void checking(C context, List<T> entities) {

    }

    default List<QueryColumn> columns(C context) {
        return null;
    }

    default void before(C context, OperateInfo<T> operateInfo) {

    }

    @Override
    default TableOneOperateConfig<C, T> config() {
        return new TableOneOperateConfig<>(
                this::using,
                this::checking,
                this::columns,
                this::before
        );
    }
}
