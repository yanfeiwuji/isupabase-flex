package io.github.yanfeiwuji.isupabase.config;

import com.mybatisflex.core.query.QueryCondition;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * @author yanfeiwuji
 * @date 2024/6/9 12:08
 */
public record RlsPolicy<T>(Supplier<QueryCondition> using,
                           Consumer<List<T>> check) {
    public static <T> RlsPolicy<T> of(Supplier<QueryCondition> using) {
        return new RlsPolicy<>(using, ls -> {
        });
    }
}
