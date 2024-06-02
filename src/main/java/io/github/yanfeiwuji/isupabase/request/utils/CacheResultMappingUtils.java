package io.github.yanfeiwuji.isupabase.request.utils;

import io.github.yanfeiwuji.isupabase.request.select.ResultMapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yanfeiwuji
 * @date 2024/6/2 17:13
 */
public class CacheResultMappingUtils {
    public static final Map<String, ResultMapping> MAPPINGS = new ConcurrentHashMap<>();


}
