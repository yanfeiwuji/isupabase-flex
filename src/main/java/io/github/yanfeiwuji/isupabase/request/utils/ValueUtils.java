package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@UtilityClass
public class ValueUtils {
    private static final Logger log = LoggerFactory.getLogger(ValueUtils.class);
    private static ObjectMapper mapper;

    public static final Map<String, Function<Object, Object>> CASTING_MAP = Map.of(
            "text", ValueUtils::castText,
            "int", ValueUtils::castInt,
            "float", ValueUtils::castFloat,
            "decimal", ValueUtils::castDecimal,
            "bool", ValueUtils::castBool);

    public static void init(ObjectMapper mapper) {
        ValueUtils.mapper = mapper;
    }

    private static final Map<String, String> IS_VALUE_MAP = Map.of(CommonStr.IS_VALUE_NULL, CommonStr.SQL_NULL,
            CommonStr.IS_VALUE_UNKNOWN, CommonStr.SQL_UNKNOWN,
            CommonStr.IS_VALUE_TRUE, CommonStr.SQL_TRUE,
            CommonStr.IS_VALUE_FALSE, CommonStr.SQL_FALSE);

    private static final Map<String, String> IS_BOOLEAN_VALUE = Map.of(CommonStr.IS_VALUE_TRUE, CommonStr.SQL_TRUE,
            CommonStr.IS_VALUE_FALSE, CommonStr.SQL_FALSE);

    public String isValue(QueryColumn queryColumn, String value) {
        String sqlIsValue = IS_VALUE_MAP.get(value);

        if (Objects.isNull(sqlIsValue)) {
            throw PgrstExFactory.exIsValueNotFound(value).get();
        }

        String isBooleanValue = IS_BOOLEAN_VALUE.get(value);

        if (Objects.nonNull(isBooleanValue)) {
            String name = queryColumn.getTable().getName();
            TableInfo tableInfo = TableInfoFactory.ofTableName(name);
            String realParam = CacheTableInfoUtils.nNRealParam(queryColumn.getName(), tableInfo);
            ColumnInfo columnInfo = CacheTableInfoUtils.nNRealColumnInfo(realParam, tableInfo);
            if (columnInfo.getPropertyType().equals(Boolean.class)) {
                String realDbType = CacheTableInfoUtils.realDbType(realParam, tableInfo);
                throw PgrstExFactory.exIsBoolButNotMatch(isBooleanValue, realDbType).get();
            }
        }
        return sqlIsValue;
    }

    public Object likeValue(QueryColumn queryColumn, String value) {
        Object o = ValueUtils.singleValue(queryColumn, value);
        return CharSequenceUtil.replace(StrUtil.toString(o), CommonStr.STAR, CommonStr.PERCENT);
    }

    public Object singleValue(QueryColumn queryColumn, String value) {

        String name = queryColumn.getTable().getName();
        TableInfo tableInfo = TableInfoFactory.ofTableName(name);
        final ObjectNode objectNode = mapper.createObjectNode();
        String realParam = CacheTableInfoUtils.nNRealParam(queryColumn.getName(), tableInfo);
        String propertyName = CacheTableInfoUtils.nNRealProperty(realParam, tableInfo);
        final ObjectNode put = objectNode.put(propertyName, value);
        try {
            Object bean = mapper.treeToValue(put, tableInfo.getEntityClass());
            return BeanUtil.getProperty(bean, propertyName);
        } catch (JsonProcessingException e) {
            String dbType = CacheTableInfoUtils.realDbType(realParam, tableInfo);
            throw PgrstExFactory.exDataInvalidInput(dbType, value).get();
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
        final ArrayNode arrayNode = mapper.createArrayNode();

        TokenUtils.splitByCommaQuoted(value)
                .stream().map(it -> mapper.createObjectNode().put(realParam, it))
                .forEach(arrayNode::add);

        Class<?> entityClass = tableInfo.getEntityClass();
        JavaType listType = mapper.getTypeFactory().constructParametricType(List.class, entityClass);

        try {
            List<?> list = mapper.treeToValue(arrayNode, listType);
            return list.stream().map(it -> BeanUtil.getProperty(it, propertyName)).toArray();
        } catch (JsonProcessingException e) {
            String dbType = CacheTableInfoUtils.realDbType(realParam, tableInfo);
            throw PgrstExFactory.exDataInvalidInput(dbType, value).get();
        }
    }

    private Object castText(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return value.toString();
    }

    private Object castInt(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return Integer.valueOf(value.toString());
    }

    private Object castFloat(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return Float.valueOf(value.toString());
    }

    private Object castDecimal(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return Double.valueOf(value.toString());
    }

    private Object castBool(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return Boolean.valueOf(value.toString());
    }

    public void checkCastKey(String castTo) {
        if (Optional.ofNullable(CASTING_MAP.get(castTo)).isEmpty()) {
            throw PgrstExFactory.exNotCasingType(castTo).get();
        }
    }

    public Object cast(String castKey, Object value) {

        return Optional.ofNullable(CASTING_MAP.get(castKey)).map(it -> it.apply(value))
                .orElse(value);
    }

    public boolean isTypeJSONArray(String str) {
        return !CharSequenceUtil.isBlank(str) && CharSequenceUtil.isWrap(CharSequenceUtil.trim(str), '[', ']');
    }
}
