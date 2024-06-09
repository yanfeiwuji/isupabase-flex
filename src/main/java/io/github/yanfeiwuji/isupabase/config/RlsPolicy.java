package io.github.yanfeiwuji.isupabase.config;

import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.query.QueryCondition;

import java.util.function.Supplier;


/**
 * @author yanfeiwuji
 * @date 2024/6/9 12:08
 */
public record RlsPolicy(String tableName,
                        OperateType operateType,
                        Supplier<QueryCondition> queryCondition) {

}
