package io.github.yanfeiwuji.isupabase.flex;

import com.mybatisflex.core.dialect.OperateType;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 18:33
 */
public record TableOneOperateConfigFor(String tableName, OperateType operateType,
                                       TableOneOperateConfig<Object> config) {
}
