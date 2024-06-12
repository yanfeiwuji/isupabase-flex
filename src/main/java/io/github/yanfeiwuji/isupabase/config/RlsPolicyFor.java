package io.github.yanfeiwuji.isupabase.config;

import com.mybatisflex.core.dialect.OperateType;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 17:45
 */
public record RlsPolicyFor(String tableName, OperateType operateType, RlsPolicy rlsPolicy) {

}
