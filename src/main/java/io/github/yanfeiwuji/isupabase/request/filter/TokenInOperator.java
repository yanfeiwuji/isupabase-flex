package io.github.yanfeiwuji.isupabase.request.filter;

import java.util.function.BiConsumer;

import com.mybatisflex.core.query.QueryWrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TokenInOperator implements IOperator {

    IN("in", (f, q) -> {
        if (f.isNegative()) {
            q.notIn(f.getRealColumn(), f.getQuantValue());
        } else {
            q.in(f.getRealColumn(), f.getQuantValue());
        }
    });

    private String mark;
    private BiConsumer<Filter, QueryWrapper> handlerFunc;
}
