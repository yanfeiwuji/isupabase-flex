package io.github.yanfeiwuji.isupabase.flex;

import com.mybatisflex.core.query.QueryCondition;


import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 12:12
 */
public record OperateInfo<T>(QueryCondition queryCondition, List<T> objects) {
}
