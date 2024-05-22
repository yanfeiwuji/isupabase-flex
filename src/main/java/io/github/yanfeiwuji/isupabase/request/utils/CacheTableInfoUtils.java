package io.github.yanfeiwuji.isupabase.request.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.NamingBase;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.relation.RelationManager;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.IdInfo;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;

import com.mybatisflex.core.table.TableInfoFactory;
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
    private static final Map<Class<?>, Map<String, AbstractRelation<?>>> CACHE_CLAZZ_PARAM_NAME_REL = new ConcurrentHashMap<>();
    private static final Map<Class<?>, String[]> CACHE_CLAZZ_RELS = new ConcurrentHashMap<>();
    private static final Map<String, TableInfo> CACHE_TABLE_INFO = new ConcurrentHashMap<>();
    private ObjectMapper mapper;
    private Optional<NamingBase> namingBaseOptional;

    public void init(ObjectMapper mapper) {
        CacheTableInfoUtils.mapper = mapper;
        CacheTableInfoUtils.namingBaseOptional = initNamingBaseOptional();
    }

    public TableInfo nNRealTableInfo(String tableName) {
        return realTableInfo(tableName)
                .orElseThrow(MDbExManagers.UNDEFINED_TABLE.supplierReqEx(tableName));
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

    public AbstractRelation<?> nNRealRelation(String paramKey, TableInfo tableInfo) {
        return realRelation(paramKey, tableInfo).orElseThrow(MDbExManagers.COLUMN_NOT_FOUND.supplierReqEx(paramKey));
    }

    public Optional<String> realColumn(String paramKey, TableInfo tableInfo) {
        return pickReal(
                paramKey,
                tableInfo,
                CACHE_CLAZZ_PARAM_NAME_COLUMN,
                () -> tableInfo.getPropertyColumnMapping()
                        .entrySet().stream().collect(
                                Collectors.toMap(it -> CacheTableInfoUtils.propertyToParamKey(it.getKey()),
                                        Map.Entry::getValue)));
    }

    public Optional<String> realProperty(String paramKey, TableInfo tableInfo) {
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_PROPERTY, () -> tableInfo.getPropertyColumnMapping()
                .entrySet().stream()
                .collect(
                        Collectors.toMap(
                                it -> CacheTableInfoUtils.propertyToParamKey(it.getKey()),
                                Map.Entry::getKey)));
    }

    public Optional<ColumnInfo> realColumnInfo(String paramKey, TableInfo tableInfo) {
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_COLUMN_INFO, () -> {
            List<ColumnInfo> columnInfos = tableInfo.getColumnInfoList();
            List<IdInfo> idInfos = tableInfo.getPrimaryKeyList();
            MapBuilder<String, ColumnInfo> builder = MapUtil.builder();
            columnInfos.forEach(info -> builder.put(
                            CacheTableInfoUtils.propertyToParamKey(info.getProperty()),
                            info
                    )
            );

            idInfos.forEach(info -> builder.put(
                    CacheTableInfoUtils.propertyToParamKey(info.getProperty()),
                    info));
            return builder.build();
        });
    }

    public Optional<QueryColumn> realQueryColumn(String paramKey, TableInfo tableInfo) {
        QueryTable queryTable = nNQueryTable(tableInfo);
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_QUERY_COLUMN, () -> tableInfo.getPropertyColumnMapping().entrySet().stream()
                .collect(
                        Collectors.toMap(
                                it -> CacheTableInfoUtils.propertyToParamKey(it.getKey()),
                                it -> new QueryColumn(queryTable, it.getValue()))));
    }

    public Optional<TableInfo> realTableInfo(String tableName) {
        return Optional.ofNullable(CACHE_TABLE_INFO.computeIfAbsent(tableName, TableInfoFactory::ofTableName));
    }

    public Optional<AbstractRelation<?>> realRelation(String paramKey, TableInfo tableInfo) {
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_REL, () ->
                RelationManager
                        .getRelations(tableInfo.getEntityClass())
                        .stream().collect(Collectors
                                .toMap(
                                        it -> CacheTableInfoUtils.propertyToParamKey(it.getRelationField().getName()),
                                        it -> it
                                )
                        )
        );
    }

    public String[] clazzRels(TableInfo tableInfo) {
        return CACHE_CLAZZ_RELS.computeIfAbsent(tableInfo.getEntityClass(),
                clazz -> RelationManager.getRelations(clazz).stream().map(it -> it.getRelationField().getName())
                        .toList().toArray(new String[]{}));
    }

    public QueryColumn nNQueryAllColumns(TableInfo tableInfo) {
        return CACHE_CLAZZ_QUERY_ALL_COLUMNS.computeIfAbsent(
                tableInfo.getEntityClass(),
                it -> new QueryColumn(nNQueryTable(tableInfo), CommonStr.STAR));
    }

    // TODO to get real dbType
    public String realDbType(String paramKey, TableInfo tableInfo) {
        ColumnInfo columnInfo = nNRealColumnInfo(paramKey, tableInfo);
        return Optional.of(columnInfo)
                .map(ColumnInfo::getJdbcType)
                .map(Enum::name)
                .orElse(columnInfo.getPropertyType().getSimpleName());
    }

    public boolean columnInTable(String selectItem, TableInfo tableInfo) {
        return realColumn(selectItem, tableInfo).isPresent();
    }

    public boolean relInTable(String selectItem, TableInfo tableInfo) {
        return realRelation(selectItem, tableInfo).isPresent();
    }

    private <T> Optional<T> pickReal(String paramKey, TableInfo tableInfo, Map<Class<?>, Map<String, T>> cacheMap,
                                     Supplier<Map<String, T>> func) {
        return Optional.ofNullable(
                cacheMap.computeIfAbsent(tableInfo.getEntityClass(), it -> func.get())
                        .get(paramKey)
        );
    }

    private Optional<NamingBase> initNamingBaseOptional() {
        return Optional.ofNullable(mapper)
                .map(ObjectMapper::getPropertyNamingStrategy)
                .filter(PropertyNamingStrategies.NamingBase.class::isInstance)
                .map(PropertyNamingStrategies.NamingBase.class::cast);
    }

    private String propertyToParamKey(String property) {
        return Optional.ofNullable(property)
                .flatMap(it -> namingBaseOptional.map(naming -> naming.translate(it)))
                .orElse(property);
    }

}