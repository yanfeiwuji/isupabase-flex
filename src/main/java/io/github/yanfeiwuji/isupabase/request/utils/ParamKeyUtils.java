package io.github.yanfeiwuji.isupabase.request.utils;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class ParamKeyUtils {
    public static final String SELECT_KEY = "select";
    public static final Map<String, String> IGNORE_FILTER = Stream.of(SELECT_KEY)
            .collect(Collectors.toMap(it -> it, it -> it));

    public boolean canFilter(String key) {
        return !IGNORE_FILTER.containsKey(key);
    }
}
