package io.github.yanfeiwuji.isupabase.request.flex.policy;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.auth.utils.ServletUtils;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstEx;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.flex.TableSetting;

import java.util.Arrays;


/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:38
 */
public abstract class PolicyBase<T> {

    protected QueryCondition EMPTY_CONDITION = QueryCondition.createEmpty();


    abstract TableSetting<T> config();


    protected void deniedOnRow() {
        throw PgrstExFactory.rowSecurityError(ServletUtils.tableName()).get();
    }

    protected PgrstEx deniedOnRowEx() {
        return PgrstExFactory.rowSecurityError(ServletUtils.tableName()).get();
    }


    protected void deniedOnCol() {
        throw PgrstExFactory.columnSecurityError(ServletUtils.tableName()).get();
    }

    protected PgrstEx deniedOnColEx() {
        return PgrstExFactory.columnSecurityError(ServletUtils.tableName()).get();
    }

    public QueryCondition and(QueryCondition... queryConditions) {
        return Arrays.stream(queryConditions).reduce(QueryCondition.createEmpty(), QueryCondition::and);
    }

    public QueryCondition or(QueryCondition... queryConditions) {
        return Arrays.stream(queryConditions).reduce(QueryCondition.createEmpty(), QueryCondition::or);
    }

}
