package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.request.ex.MDbExManagers;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ValueUtils {
    private static ObjectMapper mapper;

    public static void init(ObjectMapper mapper) {
        ValueUtils.mapper = mapper;
    }

    public Object singleValue(QueryColumn queryColumn, String value) {
        String name = queryColumn.getTable().getName();
        TableInfo tableInfo = TableInfoFactory.ofTableName(name);
        String realParam = CacheTableInfoUtils.nNRealParam(queryColumn.getName(), tableInfo);
        String propertyName = CacheTableInfoUtils.nNRealProperty(realParam, tableInfo);
        String jsonStr = new JSONObject().set(realParam, value).toString();

        try {
            Object bean = mapper.readValue(jsonStr, tableInfo.getEntityClass());
            return BeanUtil.getProperty(bean, propertyName);
        } catch (JsonProcessingException e) {
            ColumnInfo columnInfo = CacheTableInfoUtils.nNRealColumnInfo(realParam, tableInfo);
            throw MDbExManagers.INVALID_INPUT
                    .reqEx(columnInfo.getPropertyType().getSimpleName(), value);
        }
    }

    public Object[] listValueParentheses(QueryColumn queryColumn, String value) {
        return listValue(queryColumn, value, "(", ")");
    }

    private Object[] listValue(QueryColumn queryColumn, String value, String prefix, String suffix) {
        String name = queryColumn.getTable().getName();
        value = CharSequenceUtil.strip(value, prefix, suffix);

        TableInfo tableInfo = TableInfoFactory.ofTableName(name);
        String realParam = CacheTableInfoUtils.nNRealParam(queryColumn.getName(), tableInfo);
        String propertyName = CacheTableInfoUtils.nNRealProperty(realParam, tableInfo);

        JSONArray jsonArray = new JSONArray();
        TokenUtils.splitByCommaQuoted(value)
                .stream().map(it -> new JSONObject().set(realParam, it))
                .forEach(jsonArray::put);

        String json = jsonArray.toString();
        Class<?> entityClass = tableInfo.getEntityClass();
        JavaType listType = mapper.getTypeFactory().constructParametricType(List.class, entityClass);

        try {
            List<?> list = mapper.readValue(json, listType);
            return list.stream().map(it -> BeanUtil.getProperty(it, propertyName)).toArray();
        } catch (JsonProcessingException e) {
            ColumnInfo columnInfo = CacheTableInfoUtils.nNRealColumnInfo(realParam, tableInfo);
            throw MDbExManagers.INVALID_INPUT
                    .reqEx(columnInfo.getPropertyType().getSimpleName(), value);
        }


    }
}
