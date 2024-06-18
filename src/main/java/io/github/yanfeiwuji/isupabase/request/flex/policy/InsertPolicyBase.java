package io.github.yanfeiwuji.isupabase.request.flex.policy;

import com.mybatisflex.core.query.QueryColumn;
import io.github.yanfeiwuji.isupabase.request.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.request.flex.OperateInfo;
import io.github.yanfeiwuji.isupabase.request.flex.TableOneOperateConfig;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:40
 */
public class InsertPolicyBase<C extends AuthContext, T> extends PolicyBase<C, T> {
    public void checking(C context, List<T> entities) {

    }

    public List<QueryColumn> columns(C context) {
        return null;
    }

    public void before(C context, OperateInfo<T> operateInfo) {

    }

    @Override
    TableOneOperateConfig<C, T> config() {
        return new TableOneOperateConfig<>(
                (context) -> EMPTY_CONDITION,
                this::checking,
                this::columns,
                this::before
        );
    }

    ;
}
