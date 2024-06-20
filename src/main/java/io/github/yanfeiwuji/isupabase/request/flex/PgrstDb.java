package io.github.yanfeiwuji.isupabase.request.flex;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.exception.FlexExceptions;
import com.mybatisflex.core.exception.locale.LocalizedFormats;
import com.mybatisflex.core.query.*;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.row.RowUtil;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.RequiredArgsConstructor;

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

    public void load(Map<String, Map<OperateType, TableSetting<Object>>> tableSettingMap) {
        TABLE_CONFIG_MAP.putAll(tableSettingMap);
    }

    // ---- select // todo fill select and from by baseMapper
    public <T> List<T> selectListByQuery(BaseMapper<T> baseMapper, QueryWrapper queryWrapper) {

        final QueryWrapper qw = applyCondition(queryWrapper, OperateType.SELECT);
        applySelectColumns(qw);
        return baseMapper.selectListByQuery(qw);
    }

    public <T> long selectCountByQuery(BaseMapper<T> baseMapper, QueryWrapper queryWrapper) {
        return baseMapper.selectCountByQuery(applyCondition(queryWrapper, OperateType.SELECT));
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
        applyBefore(baseMapper, OperateType.INSERT, QueryCondition.createEmpty(), List.of(entity));
        final int res = baseMapper.insertSelective(entity);
        applyAfter(baseMapper, OperateType.INSERT, QueryCondition.createEmpty(), List.of(entity));
        return res;
    }

    public <T> long insertBatch(BaseMapper<T> baseMapper, List<T> entity) {

        applyInsertColumnsOnEntities(baseMapper, entity);
        applyCheck(baseMapper, OperateType.INSERT, entity);
        applyBefore(baseMapper, OperateType.INSERT, QueryCondition.createEmpty(), entity);
        final int res = baseMapper.insertBatch(entity);
        applyAfter(baseMapper, OperateType.INSERT, QueryCondition.createEmpty(), entity);
        return res;
    }

    // --- update

    public <T> long updateRowByQuery(BaseMapper<T> baseMapper, Row row, QueryWrapper queryWrapper) {
        final TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());

        applyUpdateColumnsOnRow(tableInfo.getTableNameWithSchema(), row);

        final List<T> entity = List.of((T) RowUtil.toEntity(row, tableInfo.getEntityClass()));
        applyCheck(baseMapper, OperateType.UPDATE, entity);

        QueryWrapper need = applyCondition(queryWrapper, OperateType.UPDATE);

        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(need);

        final Object[] conditionParams = CPI.getConditionParams(whereQueryCondition);

        if (Objects.isNull(conditionParams) || conditionParams.length == 0) {
            throw FlexExceptions.wrap(LocalizedFormats.UPDATE_OR_DELETE_NOT_ALLOW);
        }

        applyBefore(baseMapper, OperateType.UPDATE, whereQueryCondition, entity);
        final QueryColumn idColumn = CacheTableInfoUtils.nNRealTableIdColumn(tableInfo);

        final QueryWrapper endQueryWrapper = QueryWrapper.create().select(idColumn)
                .from(tableInfo.getEntityClass())
                .where(whereQueryCondition);
        final QueryWrapper end = QueryWrapper.create()
                .select(QueryMethods.column(PgrstStrPool.UPDATE_TEMP_TABLE, idColumn.getName()))
                .from(endQueryWrapper).as(PgrstStrPool.UPDATE_TEMP_TABLE);

        int res = Db.updateByCondition(tableInfo.getSchema(), tableInfo.getTableName(), row, idColumn.in(end));
        // todo make it more function
        applyAfter(baseMapper, OperateType.UPDATE, whereQueryCondition, entity);
        return res;
    }

    public <T> long updateByQuery(BaseMapper<T> baseMapper, T entity, QueryWrapper queryWrapper) {
        final TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());
        final Map<String, String> propertyColumnMapping = tableInfo.getPropertyColumnMapping();
        return this.updateRowByQuery(baseMapper, (Row) BeanUtil.beanToMap(entity, new Row(), true, propertyColumnMapping::get), queryWrapper);
    }

    public <T> long updateByCondition(BaseMapper<T> baseMapper, T entity, QueryCondition queryCondition) {
        return this.updateByQuery(baseMapper, entity, conditionToWrapper(baseMapper, queryCondition));
    }

    public <T> long update(BaseMapper<T> baseMapper, T entity) {
        final TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());
        final Object[] pkSqlArgs = tableInfo.buildPkSqlArgs(entity);
        if (pkSqlArgs == null || pkSqlArgs.length == 0) {
            return 0;
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
    public <T> long deleteByQuery(BaseMapper<T> baseMapper, QueryWrapper queryWrapper) {

        final TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());
        final QueryColumn idColumn = CacheTableInfoUtils.nNRealTableIdColumn(tableInfo);

        QueryWrapper need = applyCondition(queryWrapper, OperateType.DELETE);

        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(need);
        final Object[] conditionParams = CPI.getConditionParams(whereQueryCondition);

        if (Objects.isNull(conditionParams) || conditionParams.length == 0) {
            throw FlexExceptions.wrap(LocalizedFormats.UPDATE_OR_DELETE_NOT_ALLOW);
        }
        final QueryWrapper endQueryWrapper = QueryWrapper.create().select(idColumn)
                .from(tableInfo.getEntityClass())
                .where(whereQueryCondition);
        final QueryWrapper end = QueryWrapper.create().select(QueryMethods.column(PgrstStrPool.DELETE_TEMP_TABLE, idColumn.getName())).from(endQueryWrapper).as(PgrstStrPool.DELETE_TEMP_TABLE);

        applyBefore(baseMapper, OperateType.DELETE, whereQueryCondition, List.of());
        final int res = baseMapper.deleteByCondition(idColumn.in(end));
        applyAfter(baseMapper, OperateType.DELETE, whereQueryCondition, List.of());
        return res;
    }

    // --- delete
    public <T> long deleteByCondition(BaseMapper<T> baseMapper, QueryCondition queryCondition) {

        return this.deleteByQuery(baseMapper, conditionToWrapper(baseMapper, queryCondition));
    }

    private QueryWrapper applyCondition(QueryWrapper queryWrapper, OperateType operateType) {
        final QueryWrapper clone = queryWrapper.clone();
        CPI.getQueryTables(queryWrapper)
                .stream().map(QueryTable::getNameWithSchema)
                .map(it -> pickSetting(it, operateType, TableSetting::getUsing))
                .forEach(using -> using.ifPresent(it -> applyUsing(clone, it)));
        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(queryWrapper);
        applySelectSubCondition(selectConditions(whereQueryCondition, new ArrayList<>()));
        return clone;
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
                    final List<String> clear = propertyColumnMapping.entrySet().stream()
                            .filter(it -> !allowsSet.contains(it.getValue()))
                            .map(Map.Entry::getKey)
                            .toList();
                    // todo might to more high
                    entities.forEach(entity -> clear.forEach(it -> BeanUtil.setProperty(entity, it, null)));

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

                    final List<String> list = row.keySet().stream().filter(key -> !allowsSet.contains(key)).toList();
                    list.forEach(row::remove);
                    if (row.isEmpty()) {
                        // row is empty db is error
                        throw PgrstExFactory.COLUMN_SECURITY_ERROR;
                    }

                });

    }

    private void applySelectColumns(QueryWrapper queryWrapper) {
        final List<QueryColumn> columns = CPI.getSelectColumns(queryWrapper);

        final Map<String, Optional<List<QueryColumn>>> config = columns.stream().map(QueryColumn::getTable)
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

    private <T> void applyBefore(BaseMapper<T> baseMapper, OperateType operateType, QueryCondition queryCondition, List<T> objects) {
        pickSetting(baseMapperToTableNameWithSchema(baseMapper), operateType, TableSetting::getBefore)
                .ifPresent(it -> it.setting.accept(it.context, new OperateInfo<>(queryCondition.clone(), (List<Object>) objects)));
    }

    private <T> void applyAfter(BaseMapper<T> baseMapper, OperateType operateType, QueryCondition queryCondition, List<T> objects) {
        pickSetting(baseMapperToTableNameWithSchema(baseMapper), operateType, TableSetting::getAfter)
                .ifPresent(it -> it.setting.accept(it.context, new OperateInfo<>(queryCondition.clone(), (List<Object>) objects)));
    }

    private <T> String baseMapperToTableNameWithSchema(BaseMapper<T> baseMapper) {
        return TableInfoFactory.ofMapperClass(baseMapper.getClass())
                .getTableNameWithSchema();
    }

}
