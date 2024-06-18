package io.github.yanfeiwuji.isupabase.flex.policy;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.flex.OperateInfo;
import io.github.yanfeiwuji.isupabase.flex.TableOneOperateConfig;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:37
 */

public abstract class AllPolicyBase<C extends AuthContext, T> extends PolicyBase<C, T> {
    public QueryCondition using(C context) {
        return QueryCondition.createEmpty();
    }

    public void checking(C context, List<T> entities) {

    }

    public List<QueryColumn> columns(C context) {
        return null;
    }

    public void before(C context, OperateInfo<T> operateInfo) {

    }

    TableOneOperateConfig<C, T> config() {
        return new TableOneOperateConfig<>(
                this::using,
                this::checking,
                this::columns,
                this::before
        );
    }
}
