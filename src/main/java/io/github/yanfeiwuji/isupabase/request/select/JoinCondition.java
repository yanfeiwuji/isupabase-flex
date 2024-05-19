package io.github.yanfeiwuji.isupabase.request.select;

import com.mybatisflex.core.query.QueryCondition;

public record JoinCondition(Class<?> clazz, QueryCondition queryCondition) {

}
