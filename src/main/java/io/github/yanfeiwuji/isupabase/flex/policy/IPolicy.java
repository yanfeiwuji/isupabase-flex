package io.github.yanfeiwuji.isupabase.flex.policy;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.flex.TableOneOperateConfig;



/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:38
 */
public interface IPolicy<C extends AuthContext, T> {

    QueryCondition EMPTY_CONDITION = QueryCondition.createEmpty();

    TableOneOperateConfig<C, T> config();

}
