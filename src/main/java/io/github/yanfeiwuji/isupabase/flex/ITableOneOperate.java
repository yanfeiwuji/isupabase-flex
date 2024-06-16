package io.github.yanfeiwuji.isupabase.flex;

import com.mybatisflex.core.query.QueryCondition;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 12:14
 */
public interface ITableOneOperate<T> {

    default QueryCondition using(InvokeContext context) {
        return QueryCondition.createEmpty();
    }

    default void checking(InvokeContext context, List<T> entities) {

    }

    default List<String> columns(InvokeContext context) {
        return null;
    }

    default void before(InvokeContext context, OperateInfo<T> operateInfo) {

    }

    default TableOneOperateConfig<T> toConfig() {
        return new TableOneOperateConfig<>(
                this::using,
                this::checking,
                this::columns,
                this::before
        );
    }
}
