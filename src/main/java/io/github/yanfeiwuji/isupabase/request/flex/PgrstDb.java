package io.github.yanfeiwuji.isupabase.request.flex;

import ch.qos.logback.core.util.StringUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.annotation.UpdateListener;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.exception.FlexExceptions;
import com.mybatisflex.core.exception.locale.LocalizedFormats;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import com.mybatisflex.core.query.*;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.row.RowUtil;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.IdInfo;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.config.ISupabaseProperties;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.request.event.PgrstDbEvent;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yanfeiwuji
 * @date 2024/6/19 09:15
 */
@RequiredArgsConstructor
public class PgrstDb {
    private final Supplier<PgrstContext> contextSupplier;
    private Map<String, Map<OperateType, TableSetting<Object>>> TABLE_CONFIG_MAP = new ConcurrentHashMap<>();
    private final ISupabaseProperties iSupabaseProperties;
    private final ApplicationEventPublisher publisher;


    public void load(Map<String, Map<OperateType, TableSetting<Object>>> tableSettingMap) {
        TABLE_CONFIG_MAP.putAll(tableSettingMap);
    }

    // use in other option to pre query
    private <T> List<T> selectListByQueryWithType(BaseMapper<T> baseMapper, QueryWrapper queryWrapper, OperateType operateType) {
        final QueryWrapper needQueryWrapper = preHandler(baseMapper, queryWrapper, operateType);
        applySelectColumns(needQueryWrapper);
        return baseMapper.selectListByQuery(needQueryWrapper);
    }

    public <T, R> List<R> selectListByQueryAs(BaseMapper<T> baseMapper, QueryWrapper queryWrapper, Class<R> asType) {
        final QueryWrapper needQueryWrapper = preHandler(baseMapper, queryWrapper, OperateType.SELECT);
        applySelectColumns(needQueryWrapper);
        return baseMapper.selectListByQueryAs(needQueryWrapper, asType);
    }

    public <T> List<T> selectListByQuery(BaseMapper<T> baseMapper, QueryWrapper queryWrapper) {
        final QueryWrapper needQueryWrapper = preHandler(baseMapper, queryWrapper, OperateType.SELECT);
        applySelectColumns(needQueryWrapper);
        return baseMapper.selectListByQuery(needQueryWrapper);
    }

    public <T> long selectCountByQuery(BaseMapper<T> baseMapper, QueryWrapper queryWrapper) {
        final QueryWrapper needQueryWrapper = preHandler(baseMapper, queryWrapper, OperateType.SELECT);
        return baseMapper.selectCountByQuery(needQueryWrapper);
    }

    public <T> T selectOneByQuery(BaseMapper<T> baseMapper, QueryWrapper queryWrapper) {
        final QueryWrapper needQueryWrapper = preHandler(baseMapper, queryWrapper, OperateType.SELECT);
        needQueryWrapper.limit(1);
        applySelectColumns(needQueryWrapper);
        return baseMapper.selectOneByQuery(needQueryWrapper);
    }

    public <T> T selectOneByCondition(BaseMapper<T> baseMapper, QueryCondition queryCondition) {
        return this.selectOneByQuery(baseMapper, conditionToWrapper(baseMapper, queryCondition));
    }

    public <T> List<T> selectListByCondition(BaseMapper<T> baseMapper, QueryCondition queryCondition) {
        return this.selectListByQuery(baseMapper, conditionToWrapper(baseMapper, queryCondition));
    }

    public <T> long selectCountByCondition(BaseMapper<T> baseMapper, QueryCondition queryCondition) {
        return this.selectCountByQuery(baseMapper, conditionToWrapper(baseMapper, queryCondition));
    }


    // --- insert
    public <T> long insertSelective(BaseMapper<T> baseMapper, T entity) {

        applyInsertColumnsOnEntity(baseMapper, entity);
        applyCheck(baseMapper, OperateType.INSERT, List.of(entity));
        final TableInfo tableInfo = mapperToTableInfo(baseMapper);

        publisher.publishEvent(PgrstDbEvent.ofInsertBefore(this, tableInfo.getTableNameWithSchema(), List.of(entity)));
        final int res = baseMapper.insertSelective(entity);
        publisher.publishEvent(PgrstDbEvent.ofInsertAfter(this, tableInfo.getTableNameWithSchema(), List.of(entity)));
        return res;
    }

    public <T> long insertBatch(BaseMapper<T> baseMapper, List<T> entities) {

        applyInsertColumnsOnEntities(baseMapper, entities);
        applyCheck(baseMapper, OperateType.INSERT, entities);

        final TableInfo tableInfo = mapperToTableInfo(baseMapper);

        publisher.publishEvent(PgrstDbEvent.ofInsertBefore(this, tableInfo.getTableNameWithSchema(), entities));
        final int res = baseMapper.insertBatch(entities);
        publisher.publishEvent(PgrstDbEvent.ofInsertAfter(this, tableInfo.getTableNameWithSchema(), entities));
        return res;
    }

    // --- update
    public <T> List<T> updateRowByQuery(BaseMapper<T> baseMapper, Row row, QueryWrapper queryWrapper) {
        final TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());

        applyUpdateColumnsOnRow(tableInfo.getTableNameWithSchema(), row);
        final Object needHandlerOnUpdate = RowUtil.toEntity(row, tableInfo.getEntityClass());
        // handler update listeners

        CacheTableInfoUtils.nNUpdateListeners(tableInfo).forEach(it -> it.onUpdate(needHandlerOnUpdate));

        // if listeners is set some field is null  than set row is null else not handler
        final Map<String, Object> handlerAfter = BeanUtil.beanToMap(needHandlerOnUpdate, true, false);
        // handler field
        row.keySet().forEach(k -> row.replace(k, handlerAfter.get(k)));
        // add ext field
        handlerAfter.entrySet().stream().filter(entry -> Objects.nonNull(entry.getValue()))
                .forEach(entry -> row.putIfAbsent(entry.getKey(), entry.getValue()));

        // handler jackson not work
        row.forEach((k, v) -> CacheTableInfoUtils.columnJacksonTypeHandler(k, tableInfo).ifPresent(handler -> {
            try {
                row.replace(k, JacksonTypeHandler.getObjectMapper().writeValueAsString(v));
            } catch (JsonProcessingException e) {
                // copy from flex
                throw FlexExceptions.wrap(e, "Can not convert object to Json by JacksonTypeHandler: " + v);
            }
        }));

        final List<T> needUpdates = selectListByQueryWithType(baseMapper, queryWrapper, OperateType.UPDATE);

        if (needUpdates.isEmpty()) {
            return needUpdates;
        }
        final QueryColumn idCol = CacheTableInfoUtils.nNRealTableIdColumn(tableInfo);
        final List<?> needUpdateIds = entitiesToIds(tableInfo, needUpdates);
        //
        if (needUpdateIds.isEmpty()) {
            return needUpdates;
        }

        final Map<String, String> columnPropertyMapping = CacheTableInfoUtils.nNTableColumnPropertyMapping(tableInfo);

        // might handler some copy a list
        final List<T> newList = needUpdates.stream().toList();

        newList.forEach(it -> row.forEach((k, v) -> BeanUtil.setProperty(it, columnPropertyMapping.get(k), v)));

        publisher.publishEvent(PgrstDbEvent.ofUpdateBefore(this, tableInfo.getTableNameWithSchema(), needUpdates, newList));

        Db.updateByCondition(tableInfo.getSchema(), tableInfo.getTableName(), row, idCol.in(needUpdateIds));

        final List<T> dbUpdates = baseMapper.selectListByCondition(idCol.in(needUpdateIds));
        publisher.publishEvent(PgrstDbEvent.ofUpdateAfter(this, tableInfo.getTableNameWithSchema(), needUpdates, dbUpdates));
        return dbUpdates;
    }

    public <T> List<T> updateByQuery(BaseMapper<T> baseMapper, T entity, QueryWrapper queryWrapper) {
        final TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());
        Row row = (Row) BeanUtil.beanToMap(entity, new Row(), true, tableInfo::getColumnByProperty);
        // use real handler to handler

        return this.updateRowByQuery(baseMapper, row, queryWrapper);
    }

    public <T> List<T> updateByCondition(BaseMapper<T> baseMapper, T entity, QueryCondition queryCondition) {
        return this.updateByQuery(baseMapper, entity, conditionToWrapper(baseMapper, queryCondition));
    }

    public <T> List<T> update(BaseMapper<T> baseMapper, T entity) {
        final TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());
        final Object[] pkSqlArgs = tableInfo.buildPkSqlArgs(entity);
        if (pkSqlArgs == null || pkSqlArgs.length == 0) {
            return List.of(entity);
        }
        final QueryColumn idColumn = CacheTableInfoUtils.nNRealTableIdColumn(tableInfo);
        QueryCondition queryCondition;
        if (pkSqlArgs.length == 1) {
            queryCondition = idColumn.eq(pkSqlArgs[0]);
        } else {
            queryCondition = idColumn.eq(pkSqlArgs);
        }
        return this.updateByCondition(baseMapper, entity, queryCondition);
    }

    // --- delete
    public <T> List<T> deleteByQuery(BaseMapper<T> baseMapper, QueryWrapper queryWrapper) {
        final TableInfo tableInfo = mapperToTableInfo(baseMapper);

        final List<T> needDeletes = selectListByQueryWithType(baseMapper, queryWrapper, OperateType.DELETE);

        if (needDeletes.isEmpty()) {
            // null not publish anything
            return needDeletes;
        }
        List needDeleteIds = entitiesToIds(tableInfo, needDeletes);

        publisher.publishEvent(PgrstDbEvent.ofDeleteBefore(this, tableInfo.getTableNameWithSchema(), needDeletes));
        baseMapper.deleteBatchByIds(needDeleteIds);
        publisher.publishEvent(PgrstDbEvent.ofDeleteAfter(this, tableInfo.getTableNameWithSchema(), needDeletes));
        return needDeletes;
    }

    // --- delete
    public <T> List<T> deleteByCondition(BaseMapper<T> baseMapper, QueryCondition queryCondition) {

        return this.deleteByQuery(baseMapper, conditionToWrapper(baseMapper, queryCondition));
    }

    private QueryWrapper applyCondition(QueryWrapper queryWrapper, OperateType operateType) {

        CPI.getQueryTables(queryWrapper)
                .stream().map(QueryTable::getNameWithSchema)
                .map(it -> pickSetting(it, operateType, TableSetting::getUsing))
                .forEach(using -> using.ifPresent(it -> applyUsing(queryWrapper, it)));
        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(queryWrapper);
        applySelectSubCondition(selectConditions(whereQueryCondition, new ArrayList<>()));
        return queryWrapper;
    }

    private void applySelectSubCondition(List<OperatorSelectCondition> selectConditions) {
        selectConditions.stream().map(OperatorSelectCondition::getQueryWrapper)
                .filter(Objects::nonNull)
                .filter(it -> CollUtil.isNotEmpty(CPI.getQueryTables(it)))
                .forEach(queryWrapper -> CPI.getQueryTables(queryWrapper).forEach(table -> {
                    table.getNameWithSchema();
                    pickSetting(table.getNameWithSchema(), OperateType.SELECT, TableSetting::getUsing)
                            .ifPresent(it -> applyUsing(queryWrapper, it));
                }));
    }

    private <T> void applyInsertColumnsOnEntities(BaseMapper<T> baseMapper, List<T> entities) {
        if (Objects.isNull(entities) || entities.isEmpty()) {
            return;
        }
        final TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());
        final Map<String, String> propertyColumnMapping = tableInfo.getPropertyColumnMapping();
        pickSetting(tableInfo.getTableNameWithSchema(), OperateType.INSERT, TableSetting::getColumns)
                .map(it -> it.setting.apply(it.context))
                .ifPresent(allows -> {
                    final Set<String> allowsSet = allows.stream().map(QueryColumn::getName).collect(Collectors.toSet());
                    final boolean hasNotAllow = propertyColumnMapping.keySet()
                            .stream()
                            .anyMatch(it -> !allowsSet.contains(it));
                    if (hasNotAllow) {
                        throw PgrstExFactory.columnSecurityError(tableInfo.getTableNameWithSchema()).get();
                    }

                });

    }


    private <T> void applyInsertColumnsOnEntity(BaseMapper<T> baseMapper, T entity) {
        if (Objects.isNull(entity)) {
            return;
        }

        final TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());
        final Map<String, String> propertyColumnMapping = tableInfo.getPropertyColumnMapping();
        pickSetting(tableInfo.getTableNameWithSchema(), OperateType.INSERT, TableSetting::getColumns)
                .map(it -> it.setting.apply(it.context))
                .ifPresent(allows -> {
                    final Set<String> allowsSet = allows.stream().map(QueryColumn::getName).collect(Collectors.toSet());
                    final List<String> clear = propertyColumnMapping.entrySet().stream()
                            .filter(it -> !allowsSet.contains(it.getValue()))
                            .map(Map.Entry::getKey)
                            .toList();
                    clear.forEach(it -> BeanUtil.setProperty(entity, it, null));

                });
    }

    private void applyUpdateColumnsOnRow(String tableNameWithSchema, Row row) {

        pickSetting(tableNameWithSchema, OperateType.UPDATE, TableSetting::getColumns)
                .map(it -> it.setting.apply(it.context))
                .ifPresent(allows -> {
                    final Set<String> allowsSet = allows.stream().map(QueryColumn::getName).collect(Collectors.toSet());
                    //   final List<String> list = row.keySet().stream().filter(key -> !allowsSet.contains(key)).toList();
                    // if contains then throw error
                    if (row.keySet().stream().anyMatch(key -> !allowsSet.contains(key))) {
                        throw PgrstExFactory.columnSecurityError(tableNameWithSchema).get();
                    }
                });

    }

    private void applySelectColumns(QueryWrapper queryWrapper) {
        final List<QueryColumn> columns = CPI.getSelectColumns(queryWrapper);

        final Map<String, Optional<List<QueryColumn>>> config = columns.stream().map(QueryColumn::getTable)
                .filter(Objects::nonNull)
                .map(QueryTable::getNameWithSchema)
                .distinct()
                .collect(Collectors.toMap(it -> it, it -> pickSetting(it, OperateType.SELECT, TableSetting::getColumns)
                        .map(tuple -> tuple.setting.apply(tuple.context))
                ));

        if (config.values().stream().allMatch(Optional::isEmpty)) {
            return;
        }

        final Map<String, Optional<Set<String>>> setNameConfig = columns.stream().map(QueryColumn::getTable)
                .map(QueryTable::getNameWithSchema)
                .distinct()
                .collect(Collectors.toMap(it -> it, it -> pickSetting(it, OperateType.SELECT, TableSetting::getColumns)
                        .map(tuple -> tuple.setting.apply(tuple.context).stream().map(QueryColumn::getName).collect(Collectors.toSet()))
                ));


        Map<String, QueryColumn> queryAllColumnMap = MapUtil.newHashMap();
        columns.stream().filter(it -> it.getName().equals(PgrstStrPool.STAR))
                .forEach(it -> queryAllColumnMap.put(it.getTable().getNameWithSchema(), it));
        // query all replace
        final Stream<QueryColumn> queryAllEnd = queryAllColumnMap.entrySet().stream().flatMap(entry -> {
            final String key = entry.getKey();
            final QueryColumn value = entry.getValue();
            return config.get(key).map(Collection::stream).orElse(Stream.of(value));
        });


        final Stream<QueryColumn> queryColumnsEnd =
                columns.stream().filter(it -> !it.getName().equals(PgrstStrPool.STAR))
                        .filter(it -> setNameConfig.get(it.getTable().getNameWithSchema())
                                .map(allows -> allows.contains(it.getName())).orElse(true));
        final List<QueryColumn> list = Stream.concat(queryAllEnd, queryColumnsEnd).toList();


        CPI.setSelectColumns(queryWrapper, list);
    }

    private void applyUsing(QueryWrapper queryWrapper, Tuple<Function<PgrstContext, QueryCondition>> tuple) {
        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(queryWrapper);
        final QueryCondition apply = tuple.setting.apply(tuple.context);
        final QueryCondition link = QueryCondition.createEmpty().and(whereQueryCondition).and(apply);
        CPI.setWhereQueryCondition(queryWrapper, link);
    }

    private List<OperatorSelectCondition> selectConditions(QueryCondition queryCondition,
                                                           List<OperatorSelectCondition> selectConditions) {
        if (Objects.isNull(queryCondition)) {
            return selectConditions;
        }
        if (queryCondition instanceof OperatorSelectCondition selectCondition) {
            selectConditions.add(selectCondition);
        }
        final QueryCondition nextCondition = CPI.getNextCondition(queryCondition);
        if (Objects.nonNull(nextCondition)) {
            selectConditions(nextCondition, selectConditions);
        }
        return selectConditions;
    }


    // --- custom info
    private <S> Optional<Tuple<S>> pickSetting(String tableNameWithSchema, OperateType operateType, Function<TableSetting<Object>, S> mapping) {
        final PgrstContext context = contextSupplier.get();

        if (Objects.nonNull(context) && context.isServiceRole()) {
            return Optional.empty();
        } else {
            return Optional.of(TABLE_CONFIG_MAP).map(it -> it.get(tableNameWithSchema))
                    .map(it -> it.get(operateType))
                    .map(mapping)
                    .map(it -> new Tuple<>(context, it));
        }
    }

    private record Tuple<S>(PgrstContext context, S setting) {
    }

    private <T> QueryWrapper conditionToWrapper(BaseMapper<T> baseMapper, QueryCondition condition) {
        final TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());
        final QueryColumn queryColumn = CacheTableInfoUtils.nNQueryAllColumns(tableInfo);
        return QueryWrapper.create().from(tableInfo.getEntityClass()).select(queryColumn).where(condition);
    }

    private <T> void applyCheck(BaseMapper<T> baseMapper, OperateType operateType, List<T> objects) {
        pickSetting(baseMapperToTableNameWithSchema(baseMapper), operateType, TableSetting::getChecking)
                .ifPresent(it -> it.setting.accept(it.context, (List<Object>) objects));
    }


    private <T> String baseMapperToTableNameWithSchema(BaseMapper<T> baseMapper) {
        return TableInfoFactory.ofMapperClass(baseMapper.getClass())
                .getTableNameWithSchema();
    }

    private <T> QueryWrapper preHandler(BaseMapper<T> baseMapper, QueryWrapper queryWrapper, OperateType operateType) {
        final QueryWrapper result = queryWrapper.clone();

        // file select

        final List<QueryColumn> selectColumns = CPI.getSelectColumns(result);
        if (CollUtil.isEmpty(selectColumns)) {
            final TableInfo tableInfo = mapperToTableInfo(baseMapper);
            result.select(CacheTableInfoUtils.nNQueryAllColumns(tableInfo));
        }


        // fill query table
        final List<QueryTable> queryTables = CPI.getQueryTables(result);
        if (CollUtil.isEmpty(queryTables)) {
            final TableInfo tableInfo = mapperToTableInfo(baseMapper);
            result.from(tableInfo.getEntityClass());
        }

        // fill query condition
        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(queryWrapper);
        if (Objects.isNull(whereQueryCondition)) {
            result.where(QueryCondition.createEmpty());
        }

        // handler limit
        final Long limitRows = CPI.getLimitRows(result);
        final Long maxRows = iSupabaseProperties.getMaxRows();
        final Long needLimitRows = Optional.ofNullable(limitRows)
                .filter(l -> l <= maxRows)
                .orElse(maxRows);
        CPI.setLimitRows(result, needLimitRows);

        // apply condition
        applyCondition(result, operateType);

        return result;
    }

    private <T> TableInfo mapperToTableInfo(BaseMapper<T> baseMapper) {
        return TableInfoFactory.ofMapperClass(baseMapper.getClass());
    }


    public List<?> entitiesToIds(TableInfo tableInfo, List<?> entities) {

        final int pkSize = Optional.ofNullable(tableInfo.getPrimaryKeyList()).map(List::size).orElse(0);
        if (pkSize == 1) {
            return entities.stream().map(tableInfo::buildPkSqlArgs)
                    .filter(it -> it.length == 1)
                    .map(it -> it[0])
                    .filter(Objects::nonNull)
                    .toList();
        } else {
            return entities.stream().map(tableInfo::buildPkSqlArgs)
                    .toList();
        }
    }
}
