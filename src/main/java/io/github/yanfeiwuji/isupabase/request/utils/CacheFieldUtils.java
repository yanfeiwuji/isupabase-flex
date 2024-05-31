package io.github.yanfeiwuji.isupabase.request.utils;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

//
@UtilityClass
public class CacheFieldUtils {
    private final static Map<Class<?>, Map<String, Field>> CACHE_FIELD_MAP = new ConcurrentHashMap<>();

    public Field getField(Class<?> clazz, String fieldName) {
        return CACHE_FIELD_MAP.computeIfAbsent(clazz,
                c -> Arrays.stream(c.getFields())
                        .peek(field -> field.setAccessible(true))
                        .collect(
                                Collectors.toMap(it -> CacheTableInfoUtils.propertyToParamKey(it.getName()),
                                        it -> it)
                        )
        ).get(fieldName);
    }

}
