package io.github.yanfeiwuji.isupabase.request.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yanfeiwuji
 * @date 2024/6/6 09:54
 */
@UtilityClass
public class CacheJavaType {
    private static Map<Class<?>, JavaType> CACHE_LIST_JAVA_TYPE = new ConcurrentHashMap<>();

    public JavaType listJavaType(Class<?> clazz, ObjectMapper mapper) {
        return CACHE_LIST_JAVA_TYPE.computeIfAbsent(clazz, entityClass ->
                mapper.getTypeFactory().constructParametricType(List.class, entityClass)
        );
    }
}
