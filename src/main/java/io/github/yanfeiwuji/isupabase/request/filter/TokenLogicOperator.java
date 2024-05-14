package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
@AllArgsConstructor
public enum TokenLogicOperator implements IOperator {
    AND("and", (f, q) -> {

        Consumer<QueryWrapper> consumer = qw -> f.getFilters().forEach(
                it -> it.handler(qw));

        q.and(consumer);

    }),
    OR("or", (f, q) -> {
        Consumer<QueryWrapper> consumer = qw -> f.getFilters().forEach(
                it -> it.handler(qw));
        q.or(consumer);
    });

    private String mark;
    private BiConsumer<Filter, QueryWrapper> handlerFunc;
    // public void isLogicOp(paramKey){}
}
