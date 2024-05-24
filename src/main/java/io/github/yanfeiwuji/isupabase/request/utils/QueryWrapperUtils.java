package io.github.yanfeiwuji.isupabase.request.utils;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.yanfeiwuji.isupabase.request.range.Range;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class QueryWrapperUtils {
    public void handlerQueryWrapperRange(Range range, QueryWrapper queryWrapper) {
        Optional.ofNullable(range)
                .ifPresent(it -> {
                    Optional.ofNullable(it.limit())
                            .ifPresent(queryWrapper::limit);
                    Optional.ofNullable(it.offset())
                            .ifPresent(queryWrapper::offset);
                });
    }
}
