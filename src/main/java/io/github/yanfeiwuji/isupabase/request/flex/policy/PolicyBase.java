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
public interface PolicyBase<T> {

    TableSetting<T> config();


    default void deniedOnRow() {
        throw PgrstExFactory.rowSecurityError(ServletUtils.tableName()).get();
    }

    default PgrstEx deniedOnRowEx() {
        return PgrstExFactory.rowSecurityError(ServletUtils.tableName()).get();
    }


    default void deniedOnCol() {
        throw PgrstExFactory.columnSecurityError(ServletUtils.tableName()).get();
    }

    default PgrstEx deniedOnColEx() {
        return PgrstExFactory.columnSecurityError(ServletUtils.tableName()).get();
    }

    default QueryCondition and(QueryCondition... queryConditions) {
        return Arrays.stream(queryConditions).reduce(QueryCondition.createEmpty(), QueryCondition::and);
    }

    default QueryCondition or(QueryCondition... queryConditions) {
        return Arrays.stream(queryConditions).reduce(QueryCondition.createEmpty(), QueryCondition::or);
    }

}
