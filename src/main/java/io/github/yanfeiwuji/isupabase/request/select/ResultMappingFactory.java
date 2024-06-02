package io.github.yanfeiwuji.isupabase.request.select;

import com.jayway.jsonpath.JsonPath;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yanfeiwuji
 * @date 2024/6/2 16:57
 */
@UtilityClass
public class ResultMappingFactory {
    public static final Map<String, ResultMapping> MAPPINGS = new ConcurrentHashMap<>();

    // select had to query exec
    public List<ResultMapping> of(String select) {
        return ofNoCache(select, new ArrayList<>(), 0, "$.*.");
    }

    private List<ResultMapping> ofNoCache(String select, List<ResultMapping> res, int level, String pre) {
        TokenUtils.splitByCommaQuoted(select).forEach(it -> {
            MTokens.SELECT_WITH_SUB.keyValue(it).ifPresentOrElse(kv -> {
                ofNoCache(kv.value(), res, level + 1, pre + kv.key() + ".*.");
            }, () -> res.add(new ResultMapping(pre + it, it)));
        });

        return res;
    }
}
