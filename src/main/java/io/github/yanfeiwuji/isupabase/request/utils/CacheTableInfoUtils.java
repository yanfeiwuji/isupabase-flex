package io.github.yanfeiwuji.isupabase.request.utils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.NamingBase;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.RawQueryColumn;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.relation.RelationManager;
import com.mybatisflex.core.relation.ToManyRelation;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.IdInfo;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;

import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.ex.*;
import lombok.experimental.UtilityClass;

/**
 * TableInfoUtils
 */
@UtilityClass
public class CacheTableInfoUtils {

    private static final Map<Class<?>, Map<String, String>> CACHE_CLAZZ_PARAM_NAME_COLUMN = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, String>> CACHE_CLAZZ_PARAM_NAME_PROPERTY = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Map<String, ColumnInfo>> CACHE_CLAZZ_PARAM_NAME_COLUMN_INFO = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, QueryColumn>> CACHE_CLAZZ_PARAM_NAME_QUERY_COLUMN = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, String>> CACHE_CLAZZ_QUERY_COLUMN_NAME_PARAM_NAME = new ConcurrentHashMap<>();

    private static final Map<Class<?>, QueryTable> CACHE_CLAZZ_QUERY_TABLE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, QueryColumn> CACHE_CLAZZ_QUERY_ALL_COLUMNS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, QueryColumn> CACHE_CLAZZ_ID_COLUMN = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Map<String, AbstractRelation<?>>> CACHE_CLAZZ_PARAM_NAME_REL = new ConcurrentHashMap<>();

    private static final Map<String, String> CACHE_TO_MANY_REL_SELF_VALUE_SPLIT_BY = new ConcurrentHashMap<>();

    private static final Map<Class<?>, String[]> CACHE_CLAZZ_RELS = new ConcurrentHashMap<>();
    private static final Map<String, TableInfo> CACHE_TABLE_INFO = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> CACHE_TABLE_NAME_COLUMNS = new ConcurrentHashMap<>();

    private static final Map<String, QueryColumn> CACHE_REL_TARGET_QUERY_COLUMN = new ConcurrentHashMap<>();
    private static final Map<String, QueryColumn> CACHE_REL_SELF_QUERY_COLUMN = new ConcurrentHashMap<>();
    private static final Map<String, QueryColumn> CACHE_REL_JOIN_TARGET_QUERY_COLUMN = new ConcurrentHashMap<>();
    private static final Map<String, QueryColumn> CACHE_REL_JOIN_SELF_QUERY_COLUMN = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> CACHE_TABLE_NAME_ALL_COLUMN = new ConcurrentHashMap<>();


    private static ObjectMapper mapper;
    private static Optional<NamingBase> namingBaseOptional;

    public static void init(ObjectMapper mapper) {
        CacheTableInfoUtils.mapper = mapper;
        CacheTableInfoUtils.namingBaseOptional = initNamingBaseOptional();
    }

    public TableInfo nNRealTableInfo(String tableName) {
        return realTableInfo(tableName)
                .orElseThrow(PgrstExFactory.exTableNotFound(tableName));
    }

    public String nNRealColumn(String paramKey, TableInfo tableInfo) {
        return realColumn(paramKey, tableInfo)
                .orElseThrow(PgrstExFactory.exColumnNotFound(tableInfo, paramKey));
    }

    public QueryColumn nNRealTableIdColumn(TableInfo tableInfo) {
        // todo handler no id
        return realTableIdColumn(tableInfo).orElseThrow(PgrstExFactory.exColumnNotFound(tableInfo, ""));
    }

    public String nNRealProperty(String paramKey, TableInfo tableInfo) {
        return realProperty(paramKey, tableInfo).orElseThrow(PgrstExFactory.exColumnNotFound(tableInfo, paramKey));
    }

    public ColumnInfo nNRealColumnInfo(String paramKey, TableInfo tableInfo) {
        return realColumnInfo(paramKey, tableInfo).orElseThrow(PgrstExFactory.exColumnNotFound(tableInfo, paramKey));
    }

    public QueryTable nNQueryTable(TableInfo tableInfo) {
        return CACHE_CLAZZ_QUERY_TABLE.computeIfAbsent(tableInfo.getEntityClass(),
                it -> new QueryTable(tableInfo.getSchema(), tableInfo.getTableName()));
    }

    public String nNRealParam(String queryColumnName, TableInfo tableInfo) {
        return realParam(queryColumnName, tableInfo)
                .orElseThrow(PgrstExFactory.exColumnNotFound(tableInfo, queryColumnName));
    }

    public QueryColumn nNRealQueryColumn(String paramKey, TableInfo tableInfo) {
        return realQueryColumn(paramKey, tableInfo).orElseThrow(PgrstExFactory.exColumnNotFound(tableInfo, paramKey));
    }

    public AbstractRelation<?> nNRealRelation(String paramKey, TableInfo tableInfo) {
        return realRelation(paramKey, tableInfo)
                .orElseThrow(PgrstExFactory.exRelNotExist(tableInfo.getTableName(), paramKey));
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

    public Optional<String> endManyRelSelfValueSplitBy(AbstractRelation<?> relation) {
        if (!(relation instanceof ToManyRelation<?>)) {
            return Optional.empty();
        }
        return Optional.of(CACHE_TO_MANY_REL_SELF_VALUE_SPLIT_BY.computeIfAbsent(relation.getName(),
                it -> Optional.ofNullable(BeanUtil.getProperty(relation, "selfValueSplitBy"))
                        .map(Object::toString)
                        .orElse(CharSequenceUtil.EMPTY)));

    }

    public Optional<ColumnInfo> realColumnInfo(String paramKey, TableInfo tableInfo) {
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_COLUMN_INFO, () -> {
            List<ColumnInfo> columnInfos = tableInfo.getColumnInfoList();
            List<IdInfo> idInfos = tableInfo.getPrimaryKeyList();
            MapBuilder<String, ColumnInfo> builder = MapUtil.builder();
            columnInfos.forEach(info -> builder.put(
                    CacheTableInfoUtils.propertyToParamKey(info.getProperty()),
                    info));
            idInfos.forEach(info -> builder.put(
                    CacheTableInfoUtils.propertyToParamKey(info.getProperty()),
                    info));
            return builder.build();
        });
    }

    public Optional<QueryColumn> realQueryColumn(String paramKey, TableInfo tableInfo) {
        QueryTable queryTable = nNQueryTable(tableInfo);
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_QUERY_COLUMN,
                () -> tableInfo.getPropertyColumnMapping().entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        it -> CacheTableInfoUtils.propertyToParamKey(it.getKey()),
                                        it -> new QueryColumn(queryTable, it.getValue()))));
    }

    public Optional<TableInfo> realTableInfo(String tableName) {
        return Optional.ofNullable(CACHE_TABLE_INFO.computeIfAbsent(tableName, TableInfoFactory::ofTableName));
    }

    public Optional<QueryColumn> realTableIdColumn(TableInfo tableInfo) {
        return Optional.ofNullable(CACHE_CLAZZ_ID_COLUMN.computeIfAbsent(tableInfo.getEntityClass(),
                tableName -> {
                    final String[] primaryColumns = tableInfo.getPrimaryColumns();
                    if (primaryColumns.length == 1) {
                        return new QueryColumn(CacheTableInfoUtils.nNQueryTable(tableInfo), primaryColumns[0]);
                    } else if (primaryColumns.length >= 2) {

                        final String join = CharSequenceUtil.join(StrPool.COMMA, Arrays.stream(primaryColumns).toArray());
                        return new RawQueryColumn("( " + join + " )");
                    } else {
                        return null;
                    }
                }));
    }

    public Optional<AbstractRelation<?>> realRelation(String paramKey, TableInfo tableInfo) {
        return pickReal(paramKey, tableInfo, CACHE_CLAZZ_PARAM_NAME_REL, () -> RelationManager
                .getRelations(tableInfo.getEntityClass())
                .stream().collect(Collectors
                        .toMap(it -> CacheTableInfoUtils.propertyToParamKey(it.getRelationField().getName()),
                                it -> it)));
    }

    public QueryColumn nNRelTargetQueryColumn(AbstractRelation<?> relation) {
        return Optional.of(CACHE_REL_TARGET_QUERY_COLUMN.computeIfAbsent(relation.getName(),
                name -> {
                    TableInfo tableInfo = relation.getTargetTableInfo();
                    String column = tableInfo.getPropertyColumnMapping().get(relation.getTargetField().getName());
                    return new QueryColumn(nNQueryTable(tableInfo), column);
                })).orElseThrow(PgrstExFactory.exColumnNotFound(relation));
    }

    public QueryColumn nNRelSelfQueryColumn(AbstractRelation<?> relation) {
        return Optional.of(CACHE_REL_SELF_QUERY_COLUMN.computeIfAbsent(relation.getName(),
                        name -> {
                            TableInfo tableInfo = TableInfoFactory.ofEntityClass(relation.getSelfEntityClass());
                            String column = tableInfo.getPropertyColumnMapping().get(relation.getSelfField().getName());
                            return new QueryColumn(nNQueryTable(tableInfo), column);
                        }))
                .orElseThrow(PgrstExFactory.exColumnNotFound(relation));
    }

    public QueryColumn nNRelJoinTargetQueryColumn(AbstractRelation<?> relation) {
        return Optional.of(CACHE_REL_JOIN_TARGET_QUERY_COLUMN.computeIfAbsent(relation.getName(),
                        name -> {
                            TableInfo tableInfo = TableInfoFactory.ofTableName(relation.getJoinTable());
                            String column = relation.getJoinTargetColumn();
                            return new QueryColumn(nNQueryTable(tableInfo), column);
                        }))
                .orElseThrow(PgrstExFactory.exColumnNotFound(relation));
    }

    public QueryColumn nNRelJoinSelfQueryColumn(AbstractRelation<?> relation) {
        return Optional.of(CACHE_REL_JOIN_SELF_QUERY_COLUMN.computeIfAbsent(relation.getName(),
                        name -> {
                            TableInfo tableInfo = TableInfoFactory.ofTableName(relation.getJoinTable());
                            String column = relation.getJoinSelfColumn();
                            return new QueryColumn(nNQueryTable(tableInfo), column);
                        }))
                .orElseThrow(PgrstExFactory.exColumnNotFound(relation));
    }

    public Optional<String> realParam(String queryColumnName, TableInfo tableInfo) {
        return Optional.ofNullable(CACHE_CLAZZ_QUERY_COLUMN_NAME_PARAM_NAME
                .computeIfAbsent(tableInfo.getEntityClass(), clazz -> tableInfo.getPropertyColumnMapping()
                        .entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getValue,
                                it -> CacheTableInfoUtils.propertyToParamKey(it.getKey()))))
                .get(queryColumnName));
    }

    public String[] clazzRels(TableInfo tableInfo) {
        return CACHE_CLAZZ_RELS.computeIfAbsent(tableInfo.getEntityClass(),
                clazz -> RelationManager.getRelations(clazz).stream().map(it -> it.getRelationField().getName())
                        .toList().toArray(new String[]{}));
    }

    public Set<String> allColumns(QueryTable queryTable) {
        return CACHE_TABLE_NAME_COLUMNS.computeIfAbsent(queryTable.getName(),
                tableName -> Arrays.stream(CacheTableInfoUtils.nNRealTableInfo(tableName).getAllColumns())
                        .collect(Collectors.toSet()));
    }

    public Set<String> allColumnsWithRel(QueryTable queryTable) {
        return CACHE_TABLE_NAME_ALL_COLUMN.computeIfAbsent(queryTable.getName(), tableName -> {
            TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
            @SuppressWarnings("rawtypes") final List<AbstractRelation> relations = RelationManager.getRelations(tableInfo.getEntityClass());
            final Set<String> relKeys = relations.stream().map(AbstractRelation::getRelationField)
                    .map(Field::getName)
                    .map(CacheTableInfoUtils::propertyToParamKey)
                    .collect(Collectors.toSet());

            Set<String> res = new HashSet<>();
            res.addAll(relKeys);
            res.addAll(allColumns(queryTable));
            return res;
        });
    }

    public QueryColumn nNQueryAllColumns(TableInfo tableInfo) {
        return CACHE_CLAZZ_QUERY_ALL_COLUMNS.computeIfAbsent(
                tableInfo.getEntityClass(),
                it -> new QueryColumn(nNQueryTable(tableInfo), CommonStr.STAR));
    }

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
                        .get(paramKey));
    }

    private Optional<NamingBase> initNamingBaseOptional() {
        return Optional.ofNullable(mapper)
                .map(ObjectMapper::getPropertyNamingStrategy)
                .filter(PropertyNamingStrategies.NamingBase.class::isInstance)
                .map(PropertyNamingStrategies.NamingBase.class::cast);
    }

    public String propertyToParamKey(String property) {
        return Optional.ofNullable(property)
                .flatMap(it -> namingBaseOptional.map(naming -> naming.translate(it)))
                .orElse(property);
    }

}