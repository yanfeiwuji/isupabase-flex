package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.NumberUtil;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.yanfeiwuji.isupabase.entity.table.SysUserTableDef;
import io.github.yanfeiwuji.isupabase.request.range.Range;
import lombok.experimental.UtilityClass;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class ParamKeyUtils {
    public static final String SELECT_KEY = "select";
    public static final String LIMIT_KEY = "limit";
    public static final String OFFSET_KEY = "offset";
    public static final String ORDER_KEY = "order";
    private static final Map<String, String> IGNORE_FILTER = Stream.of(
            SELECT_KEY,
            LIMIT_KEY,
            OFFSET_KEY
    ).collect(Collectors.toMap(it -> it, it -> it));

    public boolean canSubFilter(String key, String pre) {
        Map<String, String> collect = Stream.of(LIMIT_KEY, OFFSET_KEY,ORDER_KEY)
                .map(it -> pre + StrPool.DOT + it)
                .collect(Collectors.toMap(it -> it, it -> it));
        return key.startsWith(pre) && !collect.containsKey(key);
    }

    public boolean canFilter(String key) {
        return !IGNORE_FILTER.containsKey(key);
    }

    public Range rootRange(MultiValueMap<String, String> params) {
        Integer limit = Optional.ofNullable(params.getFirst(ParamKeyUtils.LIMIT_KEY))
                .filter(NumberUtil::isNumber)
                .map(Integer::valueOf)
                .orElse(null);
        Integer offset = Optional.ofNullable(params.getFirst(ParamKeyUtils.OFFSET_KEY))
                .filter(NumberUtil::isNumber)
                .map(Integer::valueOf)
                .orElse(null);
        return new Range(limit, offset);
    }

    public Range preRange(MultiValueMap<String, String> params, String pre) {
        Integer limit = Optional.ofNullable(params.getFirst(pre + StrPool.DOT + ParamKeyUtils.LIMIT_KEY))
                .filter(NumberUtil::isNumber)
                .map(Integer::valueOf)
                .orElse(null);
        Integer offset = Optional.ofNullable(params.getFirst(pre + StrPool.DOT + ParamKeyUtils.OFFSET_KEY))
                .filter(NumberUtil::isNumber)
                .map(Integer::valueOf)
                .orElse(null);
        return new Range(limit, offset);
    }

    public QueryOrderBy preOrder(MultiValueMap<String, String> params) {
        params.getFirst(ORDER_KEY);
        return null;
    }
}
