package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.filter.Filter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExchangeUtils {
    private ObjectMapper mapper;

    public void init(ObjectMapper mapper) {
        ExchangeUtils.mapper = mapper;
    }


    public Object singleValue(Filter filter, TableInfo tableInfo) throws JsonProcessingException {
        String json = new JSONObject()
                .set(filter.getParamKey(), filter.getStrValue()).toString();
        Object obj = mapper.readValue(json, tableInfo.getEntityClass());
        return BeanUtil.getProperty(obj, filter.getRealProperty());
    }

    public List<Object> parenthesesWrapListValue(Filter filter, TableInfo tableInfo) throws JsonProcessingException {
        return listValue(filter, tableInfo, "(", ")");
    }

    public List<Object> delimWrapListValue(Filter filter, TableInfo tableInfo) throws JsonProcessingException {
        return listValue(filter, tableInfo, StrPool.DELIM_START, StrPool.DELIM_END);
    }

    public List<Object> listValue(Filter filter, TableInfo tableInfo, CharSequence prefix, CharSequence suffix) throws JsonProcessingException {
        String strValue = filter.getStrValue();
        String need = CharSequenceUtil.strip(strValue, prefix, suffix);

        JSONArray jsonArray = new JSONArray();
        Arrays.stream(need.split(StrPool.COMMA))
                .map(it -> new JSONObject().set(filter.getParamKey(), it))
                .forEach(jsonArray::put);

        String json = jsonArray.toString();
        Class<?> entityClass = tableInfo.getEntityClass();
        JavaType listType = mapper.getTypeFactory().constructParametricType(List.class, entityClass);

        List<?> list = mapper.readValue(json, listType);

        return list.stream().map(it -> BeanUtil.getProperty(it, filter.getRealProperty())).collect(Collectors.toList());
    }


}
