package io.github.yanfeiwuji.isupabase.request.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MapKeyUtils {
    public String depthRelKey(Integer depth, String relName) {
        return "depthRelKey_%d:%s".formatted(depth, relName);
    }
}
