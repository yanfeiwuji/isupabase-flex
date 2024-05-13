package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JavaType;
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
        Object obj = mapper.readValue(json, filter.getTableInfo().getEntityClass());
        return BeanUtil.getProperty(obj, filter.getRealProperty());
    }

    @SneakyThrows
    public List<Object> listValue(Filter filter) {
        String strValue = filter.getStrValue();
        String need = StrUtil.strip(strValue, StrPool.DELIM_START, StrPool.DELIM_END);

        JSONArray jsonArray = new JSONArray();
        Arrays.stream(need.split(StrPool.COMMA))
                .map(it -> new JSONObject().set(filter.getRealProperty(), it))
                .forEach(jsonArray::put);

        String json = jsonArray.toString();
        Class<?> entityClass = filter.getTableInfo().getEntityClass();
        JavaType listType = mapper.getTypeFactory().constructParametricType(List.class, entityClass);

        List<?> list = mapper.readValue(json, listType);

        return list.stream().map(it -> BeanUtil.getProperty(it, filter.getRealProperty())).collect(Collectors.toList());
    }
}
