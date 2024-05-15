package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;
import java.util.regex.Pattern;

@UtilityClass
public class MLogicOperators {
    final Operator AND = new Operator("and", Pattern.compile("^and(.*)"), (f, q) -> {
        Consumer<QueryWrapper> consumer = qw -> f.getFilters().forEach(
                it -> it.handler(qw));
        q.and(consumer);

    });
    final Operator OR = new Operator("or", Pattern.compile("^or(.*)"), (f, q) -> {
        Consumer<QueryWrapper> consumer = qw -> f.getFilters().forEach(
                it -> it.handler(qw));
        q.and(consumer);
    });
}
