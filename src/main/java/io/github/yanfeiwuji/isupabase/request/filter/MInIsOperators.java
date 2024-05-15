package io.github.yanfeiwuji.isupabase.request.filter;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class MInIsOperators {
    public final Operator IN = new Operator("in", Pattern.compile("in\\.\\((.*)\\)"), (f, q) -> {
        if (f.isNegative()) {
            q.notIn(f.getRealColumn(), f.getQuantValue());
        } else {
            q.in(f.getRealColumn(), f.getQuantValue());
        }
    });
    public final Operator IS = new Operator("is", Pattern.compile("in\\.\\((.*)\\)"), (f, q) -> {
        if (f.isNegative()) {
            q.notIn(f.getRealColumn(), f.getQuantValue());
        } else {
            q.in(f.getRealColumn(), f.getQuantValue());
        }
    });
}
