package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.filter.Filter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExchangeUtils {
    private static ObjectMapper mapper;

    public void init(ObjectMapper mapper) {
        ExchangeUtils.mapper = mapper;
    }

    @SneakyThrows
    public Object singleValue(Filter filter) {
        String json = new JSONObject()
                .set(filter.getRealProperty(), filter.getStrValue()).toString();
        return mapper.readValue(json, filter.getTableInfo().getEntityClass());
    }
}
