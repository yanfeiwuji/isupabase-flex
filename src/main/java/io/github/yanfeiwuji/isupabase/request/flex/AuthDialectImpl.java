package io.github.yanfeiwuji.isupabase.request.flex;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.dialect.KeywordWrap;
import com.mybatisflex.core.dialect.LimitOffsetProcessor;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.dialect.impl.CommonsDialectImpl;
import com.mybatisflex.core.query.*;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.row.RowUtil;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.update.UpdateWrapper;
import com.mybatisflex.core.util.StringUtil;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * option by id not do  any rls , develop should use qw to do anything
 */
@Slf4j
@RequiredArgsConstructor
public class AuthDialectImpl<C extends AuthContext> extends CommonsDialectImpl {


    private final AuthContextSupplier<C> authContextSupplier;

    private final Map<String, Map<OperateType, TableOneOperateConfig<C, Object>>> TABLE_CONFIG_MAP = new ConcurrentHashMap<>();

    public AuthDialectImpl(KeywordWrap keywordWrap,
                           LimitOffsetProcessor limitOffsetProcessor,
                           AuthContextSupplier<C> authContextSupplier
    ) {
        super(keywordWrap, limitOffsetProcessor);
        this.authContextSupplier = authContextSupplier;
    }

    public void init(Map<String, Map<OperateType, TableOneOperateConfig<C, Object>>> map) {
        TABLE_CONFIG_MAP.putAll(map);
    }


    @Override
    public void prepareAuth(QueryWrapper queryWrapper, OperateType operateType) {


        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(queryWrapper);
        applyRls(queryWrapper, operateType);
        conditions(whereQueryCondition, new ArrayList<>()).forEach(it -> {
            final QueryWrapper qw = it.getQueryWrapper();
            applyRls(qw, operateType);
        });

        switch (operateType) {
            case SELECT -> applySelectColumnsPolicy(queryWrapper);
            case DELETE -> onDelete(queryWrapper);

        }


    }


    private List<OperatorSelectCondition> conditions(QueryCondition queryCondition,
                                                     List<OperatorSelectCondition> selectConditions) {
        if (Objects.isNull(queryCondition)) {
            return selectConditions;
        }
        if (queryCondition instanceof OperatorSelectCondition selectCondition) {
            selectConditions.add(selectCondition);
        }
        final QueryCondition nextCondition = CPI.getNextCondition(queryCondition);
        if (Objects.nonNull(nextCondition)) {
            conditions(nextCondition, selectConditions);
        }
        return selectConditions;
    }

    private void applyRls(QueryWrapper queryWrapper, OperateType operateType) {
        List<QueryTable> queryTables = CPI.getQueryTables(queryWrapper);
        queryTables.stream().map(it -> using(it, operateType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(it -> it.func.apply(it.context))
                .forEach(condition -> {
                    queryWrapper.and(qw -> {
                        qw.and(condition);
                    });
                });
    }


    @Override
    public String forInsertRow(String schema, String tableName, Row row) {
        final TableInfo tableInfo = TableInfoFactory.ofTableName(getNameWithSchema(schema, tableName));
        final QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(tableInfo);
        applyInsertColumnsOnRow(queryTable, row);
        onInsert(tableInfo, () -> List.of(RowUtil.toEntity(row, tableInfo.getEntityClass())));
        return super.forInsertRow(schema, tableName, row);
    }

    @Override
    public String forInsertBatchWithFirstRowColumns(String schema, String tableName, List<Row> rows) {
        final TableInfo tableInfo = TableInfoFactory.ofTableName(getNameWithSchema(schema, tableName));
        final QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(tableInfo);
        rows.forEach(row -> applyUpdateColumnsOnRow(queryTable, row));
        onInsert(TableInfoFactory.ofTableName(getNameWithSchema(schema, tableName)), () -> {
            final Class<?> entityClass = CacheTableInfoUtils.nNRealTableInfo(getNameWithSchema(schema, tableName)).getEntityClass();
            return (List<Object>) RowUtil.toEntityList(rows, entityClass);
        });
        return super.forInsertBatchWithFirstRowColumns(schema, tableName, rows);
    }

    @Override
    public String forInsertEntity(TableInfo tableInfo, Object entity, boolean ignoreNulls) {
        applyInsertColumnsOnEntity(tableInfo, entity);
        onInsert(tableInfo, () -> List.of(entity));
        return super.forInsertEntity(tableInfo, entity, ignoreNulls);
    }

    @Override
    public String forInsertEntityWithPk(TableInfo tableInfo, Object entity, boolean ignoreNulls) {
        applyInsertColumnsOnEntity(tableInfo, entity);
        onInsert(tableInfo, () -> List.of(entity));
        return super.forInsertEntityWithPk(tableInfo, entity, ignoreNulls);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String forInsertEntityBatch(TableInfo tableInfo, List<?> entities) {
        entities.forEach(entity -> applyInsertColumnsOnEntity(tableInfo, entity));
        onInsert(tableInfo, () -> (List<Object>) entities);
        return super.forInsertEntityBatch(tableInfo, entities);
    }


    @Override
    public String forUpdateByQuery(QueryWrapper queryWrapper, Row row) {
        CPI.getQueryTables(queryWrapper).stream()
                .findFirst().ifPresent(it -> {
                    applyUpdateColumnsOnRow(it, row);
                    applyUpdateColumnsOnUpdateQueryWrapper(it, queryWrapper);
                });
        onUpdate(queryWrapper, () -> (List<Object>) CPI.getQueryTables(queryWrapper).stream()
                .findFirst()
                .map(QueryTable::getNameWithSchema)
                .map(CacheTableInfoUtils::nNRealTableInfo)
                .map(TableInfo::getEntityClass)
                .map(row::toEntity)
                .map(List::of)
                .orElse(List.of())
        );
        return super.forUpdateByQuery(queryWrapper, row);
    }


    @Override
    public String forUpdateEntityByQuery(TableInfo tableInfo, Object entity, boolean ignoreNulls,
                                         QueryWrapper queryWrapper) {


        applyUpdateColumnsOnEntity(tableInfo, entity);
        applyUpdateColumnsOnUpdateQueryWrapper(tableInfo, queryWrapper);
        onUpdate(queryWrapper, () -> toUpdateEntity(tableInfo, queryWrapper).orElse(List.of(entity)));

        return super.forUpdateEntityByQuery(tableInfo, entity, ignoreNulls, queryWrapper);
    }


    private Optional<List<Object>> toUpdateEntity(TableInfo tableInfo, QueryWrapper queryWrapper) {
        return Optional.ofNullable(queryWrapper)
                .filter(UpdateChain.class::isInstance)
                .map(UpdateChain.class::cast)
                .map(it -> (UpdateWrapper<?>) BeanUtil.getProperty(it, "entityWrapper"))
                .map(UpdateWrapper::getUpdates)
                .map(it -> {
                    final Row row = new Row();
                    it.forEach(row::set);
                    return row;
                }).map(it -> RowUtil.toEntity(it, tableInfo.getEntityClass()))
                .map(List::of);
    }

    private void onDelete(QueryWrapper queryWrapper) {
        CPI.getQueryTables(queryWrapper)
                .stream().map(this::deleteBefore)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(it -> it.func.accept(it.context, new OperateInfo<>(CPI.getWhereQueryCondition(queryWrapper), List.of())));
    }

    private void applyInsertColumnsOnRow(QueryTable queryTable, Row row) {
        insertColumns(queryTable).map(it -> it.func.apply(it.context))
                .ifPresent(allows -> allowsApplyOnRow(allows, row, queryTable.getNameWithSchema()));

    }

    private void applyInsertColumnsOnEntity(TableInfo tableInfo, Object entity) {
        final QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(tableInfo);
        insertColumns(queryTable).map(it -> it.func.apply(it.context))
                .ifPresent(allows -> allowsApplyOnEntity(tableInfo, allows, entity));
    }


    private void applyUpdateColumnsOnRow(QueryTable queryTable, Row row) {
        updateColumns(queryTable).map(it -> it.func.apply(it.context))
                .ifPresent(allows -> allowsApplyOnRow(allows, row, queryTable.getNameWithSchema()));


    }


    private void applyUpdateColumnsOnUpdateQueryWrapper(TableInfo queryTable, QueryWrapper queryWrapper) {
        applyUpdateColumnsOnUpdateQueryWrapper(CacheTableInfoUtils.nNQueryTable(queryTable), queryWrapper);
    }

    private void applyUpdateColumnsOnUpdateQueryWrapper(QueryTable queryTable, QueryWrapper queryWrapper) {

        Optional.ofNullable(queryWrapper)
                .filter(UpdateChain.class::isInstance)
                .map(UpdateChain.class::cast)
                .map(it -> (UpdateWrapper<?>) BeanUtil.getProperty(it, "entityWrapper"))
                .map(UpdateWrapper::getUpdates)
                .ifPresent(updates -> updateColumns(queryTable)
                        .map(it -> it.func.apply(it.context))
                        .ifPresent(allows -> {
                            final Map<String, String> allowMap = allows.stream().collect(Collectors.toMap(QueryColumn::getName, QueryColumn::getName));
                            updates.keySet().stream().filter(it -> !allowMap.containsKey(it)).forEach(updates::remove);
                            if (updates.isEmpty()) {
                                throw PgrstExFactory.columnSecurityError(queryTable.getNameWithSchema()).get();
                            }
                        }));

    }

    private void allowsApplyOnRow(List<QueryColumn> allows, Row row, String nameWithSchema) {
        final Map<String, String> allowMap = allows.stream().collect(Collectors.toMap(QueryColumn::getName, QueryColumn::getName));
        final Set<String> rowKeys = row.keySet();

        rowKeys.stream().filter(it -> !allowMap.containsKey(it)).forEach(row::remove);
        if (row.isEmpty()) {
            throw PgrstExFactory.columnSecurityError(nameWithSchema).get();
        }
    }


    // use entity must use ignore null other well update all columns to null
    private void applyUpdateColumnsOnEntity(TableInfo tableInfo, Object entity) {

        if (Objects.isNull(entity)) {
            return;
        } //
        final QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(tableInfo);

        updateColumns(queryTable).map(it -> it.func.apply(it.context))
                .ifPresent(allows -> allowsApplyOnEntity(tableInfo, allows, entity));

    }

    private void allowsApplyOnEntity(TableInfo tableInfo, List<QueryColumn> allows, Object entity) {
        if (Objects.isNull(entity)) {
            return;
        }
        if (!tableInfo.getEntityClass().isInstance(entity)) {
            return;
        }
        final Map<String, String> columnPropertyMapping = CacheTableInfoUtils.nNTableColumnPropertyMapping(tableInfo);
        final Map<String, String> allowMap = allows.stream().collect(Collectors.toMap(QueryColumn::getName, QueryColumn::getName));
        columnPropertyMapping.keySet().stream().filter(it -> !allowMap.containsKey(it))
                .map(columnPropertyMapping::get)
                .forEach(it -> BeanUtil.setProperty(entity, it, null));
    }

    private void onInsert(TableInfo tableInfo, Supplier<List<Object>> supplier) {
        beforeInsertCheck(tableInfo).ifPresent(it -> it.func.accept(it.context, supplier.get()));
        final QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(tableInfo);
        insertBefore(queryTable).ifPresent(it -> it.func.accept(it.context, new OperateInfo<>(QueryCondition.createEmpty(), supplier.get())));

    }

    private void onUpdate(QueryWrapper queryWrapper, Supplier<List<Object>> supplier) {

        CPI.getQueryTables(queryWrapper)
                .stream().map(this::updateChecking)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(it -> it.func.accept(it.context, supplier.get()));
        CPI.getQueryTables(queryWrapper)
                .stream().map(this::updateBefore)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(it -> it.func.accept(it.context, new OperateInfo<>(CPI.getWhereQueryCondition(queryWrapper), supplier.get())));
        ;
    }


    private void applySelectColumnsPolicy(QueryWrapper queryWrapper) {
        CPI.getQueryTables(queryWrapper)
                .stream().map(this::selectColumns)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(it -> it.func.apply(it.context))
                .filter(Objects::nonNull)
                .forEach(allowColumns -> {
                    final List<QueryColumn> needColumns = CPI.getSelectColumns(queryWrapper);
                    final boolean all = needColumns.stream().anyMatch(it -> CharSequenceUtil.equals(it.getName(), PgrstStrPool.STAR));
                    if (all) {
                        CPI.setSelectColumns(queryWrapper, allowColumns);
                    } else {
                        final List<QueryColumn> endColumns = needColumns.stream().filter(
                                it -> allowColumns.stream().anyMatch(allow -> CharSequenceUtil.equals(allow.getTable().getNameWithSchema(), it.getTable().getNameWithSchema())
                                                                              && CharSequenceUtil.equals(allow.getName(), it.getName()))).toList();
                        final List<QueryTable> queryTables = CPI.getQueryTables(queryWrapper);
                        if (endColumns.isEmpty()) {
                            // can not look any
                            // might null point
                            throw PgrstExFactory.columnSecurityError(queryTables.getFirst().getNameWithSchema()).get();
                        }
                        CPI.setSelectColumns(queryWrapper, endColumns);
                    }
                });
    }

    private Optional<Tuple<C, BiConsumer<C, OperateInfo<Object>>>> insertBefore(QueryTable queryTable) {
        return configOption(queryTable, OperateType.INSERT, TableOneOperateConfig::getBefore);
    }

    private Optional<Tuple<C, BiConsumer<C, OperateInfo<Object>>>> deleteBefore(QueryTable queryTable) {
        return configOption(queryTable, OperateType.DELETE, TableOneOperateConfig::getBefore);
    }

    private Optional<Tuple<C, Function<C, QueryCondition>>> using(QueryTable queryTable, OperateType operateType) {
        return configOption(queryTable, operateType, TableOneOperateConfig::getUsing);
    }

    private Optional<Tuple<C, Function<C, List<QueryColumn>>>> insertColumns(QueryTable queryTable) {
        return configOption(queryTable, OperateType.UPDATE, TableOneOperateConfig::getColumns);
    }

    private Optional<Tuple<C, Function<C, List<QueryColumn>>>> updateColumns(QueryTable queryTable) {
        return configOption(queryTable, OperateType.UPDATE, TableOneOperateConfig::getColumns);
    }

    private Optional<Tuple<C, Function<C, List<QueryColumn>>>> selectColumns(QueryTable queryTable) {
        return configOption(queryTable, OperateType.SELECT, TableOneOperateConfig::getColumns);
    }

    private Optional<Tuple<C, BiConsumer<C, List<Object>>>> beforeInsertCheck(TableInfo tableInfo) {
        final QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(tableInfo);
        return configOption(queryTable, OperateType.INSERT, TableOneOperateConfig::getChecking);

    }

    private Optional<Tuple<C, BiConsumer<C, OperateInfo<Object>>>> updateBefore(QueryTable queryTable) {

        return configOption(queryTable, OperateType.UPDATE, TableOneOperateConfig::getBefore);
    }

    private Optional<Tuple<C, BiConsumer<C, List<Object>>>> updateChecking(QueryTable queryTable) {

        return configOption(queryTable, OperateType.UPDATE, TableOneOperateConfig::getChecking);
    }


    private <R> Optional<Tuple<C, R>> configOption(QueryTable queryTable, OperateType operateType, Function<TableOneOperateConfig<C, Object>, R> mapping) {
        final C context = authContextSupplier.get();
        if (Objects.nonNull(context) && context.isServiceRole()) {
            return Optional.empty();
        } else {
            return Optional.of(TABLE_CONFIG_MAP).map(it -> it.get(queryTable.getNameWithSchema()))
                    .map(it -> it.get(operateType))
                    .map(mapping)
                    .map(it -> new Tuple<C, R>(context, it));
        }
    }

    private record Tuple<C, F>(C context, F func) {

    }

    public String getNameWithSchema(String schema, String name) {
        return StringUtil.isNotBlank(schema) ? schema + "." + name : name;
    }
}
