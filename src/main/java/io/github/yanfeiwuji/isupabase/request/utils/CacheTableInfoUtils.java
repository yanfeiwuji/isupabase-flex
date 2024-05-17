package io.github.yanfeiwuji.isupabase.request.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.NamingBase;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.IdInfo;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;

import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.ex.MDbExManagers;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TableInfoUtils
 */
@UtilityClass
public class CacheTableInfoUtils {

    private static final Map<Class<?>, Map<String, String>> CACHE_CLAZZ_PARAM_NAME_COLUMN = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, String>> CACHE_CLAZZ_PARAM_NAME_PROPERTY = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, ColumnInfo>> CACHE_CLAZZ_PARAM_NAME_COLUMN_INFO = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, QueryColumn>> CACHE_CLAZZ_PARAM_NAME_QUERY_COLUMN = new ConcurrentHashMap<>();
    private static final Map<Class<?>, QueryTable> CACHE_CLAZZ_QUERY_TABLE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, QueryColumn> CACHE_CLAZZ_QUERY_ALL_COLUMNS = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(CacheTableInfoUtils.class);
    private static ObjectMapper mapper;

    public void init(ObjectMapper mapper) {
        CacheTableInfoUtils.mapper = mapper;
    }

    public String nNRealColumn(String paramKey, TableInfo tableInfo) {
        return realColumn(paramKey, tableInfo).orElseThrow(MDbExManagers.COLUMN_NOT_FOUND.supplierReqEx(paramKey));
    }

    public String nNRealProperty(String paramKey, TableInfo tableInfo) {
        return realProperty(paramKey, tableInfo).orElseThrow(MDbExManagers.COLUMN_NOT_FOUND.supplierReqEx(paramKey));
    }

    public ColumnInfo nNRealColumnInfo(String paramKey, TableInfo tableInfo) {
        return realColumnInfo(paramKey, tableInfo).orElseThrow(MDbExManagers.COLUMN_NOT_FOUND.supplierReqEx(paramKey));
    }

    public QueryTable nNQueryTable(TableInfo tableInfo) {
        return CACHE_CLAZZ_QUERY_TABLE.computeIfAbsent(tableInfo.getEntityClass(),
                it -> new QueryTable(tableInfo.getSchema(), tableInfo.getTableName()));
    }

    public QueryColumn nNRealQueryColumn(String paramKey, TableInfo tableInfo) {
        return realQueryColumn(paramKey, tableInfo).orElseThrow(MDbExManagers.COLUMN_NOT_FOUND.supplierReqEx(paramKey));
    }

    public Optional<String> realColumn(String paramKey, TableInfo tableInfo) {
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_COLUMN, namingBase -> {
            Map<String, String> map = tableInfo.getPropertyColumnMapping();
            return namingBase.map(it -> {
                MapBuilder<String, String> builder = MapUtil.builder();
                map.forEach((k, v) -> builder.put(it.translate(k), v));
                return builder.build();
            }).orElse(map);
        });
    }

    public Optional<String> realProperty(String paramKey, TableInfo tableInfo) {
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_PROPERTY, namingBase -> {
            Map<String, String> map = tableInfo.getPropertyColumnMapping();
            return namingBase.map(it -> {
                MapBuilder<String, String> builder = MapUtil.builder();
                map.forEach((k, v) -> builder.put(it.translate(k), k));
                return builder.build();
            }).orElse(map);
        });
    }

    public Optional<ColumnInfo> realColumnInfo(String paramKey, TableInfo tableInfo) {
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_COLUMN_INFO, namingBase -> {
            List<ColumnInfo> columnInfos = tableInfo.getColumnInfoList();
            List<IdInfo> idInfos = tableInfo.getPrimaryKeyList();
            MapBuilder<String, ColumnInfo> builder = MapUtil.builder();
            columnInfos.forEach(info -> builder.put(
                    namingBase.map(naming -> naming.translate(info.getProperty()))
                            .orElse(info.getProperty()),
                    info));

            idInfos.forEach(info -> builder.put(
                    namingBase.map(naming -> naming.translate(info.getProperty()))
                            .orElse(info.getProperty()),
                    info));

            Map<String, ColumnInfo> build = builder.build();
            log.info("info:{}", build);
            return build;
        });
    }

    public Optional<QueryColumn> realQueryColumn(String paramKey, TableInfo tableInfo) {
        QueryTable queryTable = nNQueryTable(tableInfo);
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_QUERY_COLUMN, namingBase -> {
            Map<String, String> propertyColumnMapping = tableInfo.getPropertyColumnMapping();
            Map<String, QueryColumn> queryColumnMap = MapUtil.newConcurrentHashMap();
            propertyColumnMapping.forEach((k, v) -> queryColumnMap.put(
                    namingBase.map(naming -> naming.translate(k)).orElse(k),
                    new QueryColumn(queryTable, v)));
            return queryColumnMap;
        });

    }

    public QueryColumn nNQueryAllColumns(TableInfo tableInfo) {
        return CACHE_CLAZZ_QUERY_ALL_COLUMNS.computeIfAbsent(
                tableInfo.getEntityClass(),
                it -> new QueryColumn(nNQueryTable(tableInfo), CommonStr.STAR));
    }

    public boolean columnInTable(String selectItem, TableInfo tableInfo) {
        return realColumn(selectItem, tableInfo).isPresent();
    }

    private <T> Optional<T> pickReal(String paramKey, TableInfo tableInfo, Map<Class<?>, Map<String, T>> cacheMap,
            Function<Optional<NamingBase>, Map<String, T>> func) {
        return Optional.ofNullable(
                cacheMap.computeIfAbsent(tableInfo.getEntityClass(), it -> {
                    Optional<NamingBase> namingOptional = Optional.ofNullable(mapper)
                            .map(ObjectMapper::getPropertyNamingStrategy)
                            .filter(PropertyNamingStrategies.NamingBase.class::isInstance)
                            .map(PropertyNamingStrategies.NamingBase.class::cast);
                    return func.apply(namingOptional);
                }).get(paramKey));
    }

    // TODO to get real dbType
    public String realDbType(String paramKey, TableInfo tableInfo) {

        ColumnInfo columnInfo = nNRealColumnInfo(paramKey, tableInfo);
        return Optional.of(columnInfo)
                .map(ColumnInfo::getJdbcType)
                .map(Enum::name)
                .orElse(columnInfo.getPropertyType().getSimpleName());
    }

}