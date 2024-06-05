package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yanfeiwuji
 * @date 2024/6/5 15:54
 */
@UtilityClass
public class PreferUtils {
    private static final Map<String, Object> PREFER_MAP = Stream.of(
                    CommonStr.PREFER_HANDLING_STRICT,
                    CommonStr.PREFER_HANDLING_LENIENT,

                    CommonStr.PREFER_TIMEZONE,

                    CommonStr.PREFER_COUNT_EXACT,
                    CommonStr.PREFER_COUNT_PLANNED,
                    CommonStr.PREFER_COUNT_ESTIMATED,

                    CommonStr.PREFER_RETURN_MINIMAL,
                    CommonStr.PREFER_RETURN_HEADERS_ONLY,
                    CommonStr.PREFER_RETURN_REPRESENTATION,

                    CommonStr.PREFER_RESOLUTION_MERGE_DUPLICATES,
                    CommonStr.PREFER_RESOLUTION_IGNORE_DUPLICATES,

                    CommonStr.PREFER_MISSION_DEFAULT)
            .collect(Collectors.toMap(it -> it, it -> it));

    public Map<String, String> pickPrefer(String preferHeader) {
        if (Objects.isNull(preferHeader)) {
            return Map.of();
        }
        boolean strict = preferHeader.contains(CommonStr.PREFER_HANDLING_STRICT);

        final List<String> prefers = CharSequenceUtil.splitTrim(preferHeader, StrPool.COMMA);

        if (strict) {
            final List<String> list = prefers.stream().filter(it -> !PREFER_MAP.containsKey(it))
                    .toList();
            if (!list.isEmpty()) {
                throw PgrstExFactory.exInvalidPreferInStrict(list).get();
            }
        }
        return prefers.stream().filter(PREFER_MAP::containsKey).collect(Collectors.toMap(it -> it, it -> it));
    }

}
