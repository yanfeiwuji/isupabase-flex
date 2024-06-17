package io.github.yanfeiwuji.isupabase.flex;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.dialect.KeywordWrap;
import com.mybatisflex.core.dialect.LimitOffsetProcessor;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.dialect.impl.CommonsDialectImpl;
import com.mybatisflex.core.query.*;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.row.RowUtil;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.update.UpdateWrapper;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * option by id not do  any rls , develop should use qw to do anything
 */
@Slf4j
@RequiredArgsConstructor
public class AuthDialectImpl<C extends AuthContext> extends CommonsDialectImpl {


    private final AuthContextSupplier<C> authContextSupplier;

    private static final Map<String, Map<OperateType, TableOneOperateConfig<AuthContext, Object>>> TABLE_CONFIG_MAP = new ConcurrentHashMap<>();

    public AuthDialectImpl(KeywordWrap keywordWrap,
                           LimitOffsetProcessor limitOffsetProcessor,
                           AuthContextSupplier<C> authContextSupplier
    ) {
        super(keywordWrap, limitOffsetProcessor);
        this.authContextSupplier = authContextSupplier;
    }

    public static void init(Map<String, Map<OperateType, TableOneOperateConfig<AuthContext, Object>>> map) {
        TABLE_CONFIG_MAP.putAll(map);
    }


    @Override
    public void prepareAuth(QueryWrapper queryWrapper, OperateType operateType) {

        log.info("prepareAuth qw operateType: {}", operateType);
        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(queryWrapper);
        final C context = authContextSupplier.get();
        // todo  where server_role not handler
        applyRls(queryWrapper, operateType);
        conditions(whereQueryCondition, new ArrayList<>()).forEach(it -> {
            final QueryWrapper qw = it.getQueryWrapper();
            applyRls(qw, operateType);
        });

        if (Objects.equals(operateType, OperateType.DELETE)) {
            CPI.getQueryTables(queryWrapper).forEach(queryTable -> Optional.of(TABLE_CONFIG_MAP).map(it -> it.get(queryTable.getNameWithSchema()))
                    .map(it -> it.get(OperateType.DELETE))
                    .map(TableOneOperateConfig::getBefore)
                    .ifPresent(before -> {
                        before.accept(authContextSupplier.get(), new OperateInfo<>(CPI.getWhereQueryCondition(queryWrapper), List.of()));
                    }));
        }
        super.prepareAuth(queryWrapper, operateType);
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

        queryTables.forEach(it -> Optional.of(TABLE_CONFIG_MAP).map(map -> map.get(it.getNameWithSchema()))
                .map(map -> map.get(operateType))
                .map(TableOneOperateConfig::getUsing)
                .map(func -> func.apply(authContextSupplier.get()))
                .ifPresent(queryCondition -> queryWrapper.and(qw -> {
                            qw.and(queryCondition);
                        })
                ));
    }


    @Override
    public String forInsertRow(String schema, String tableName, Row row) {
        final Class<?> entityClass = CacheTableInfoUtils.nNRealTableInfo(tableName).getEntityClass();
        beforeInsertCheck(tableName, List.of(RowUtil.toEntity(row, entityClass)));
        return super.forInsertRow(schema, tableName, row);
    }

    @Override
    public String forInsertBatchWithFirstRowColumns(String schema, String tableName, List<Row> rows) {
        final Class<?> entityClass = CacheTableInfoUtils.nNRealTableInfo(tableName).getEntityClass();
        final List<?> entityList = RowUtil.toEntityList(rows, entityClass);
        beforeInsertCheck(tableName, (List<Object>) entityList);
        return super.forInsertBatchWithFirstRowColumns(schema, tableName, rows);
    }

    @Override
    public String forInsertEntity(TableInfo tableInfo, Object entity, boolean ignoreNulls) {
        beforeInsertCheck(tableInfo.getTableName(), List.of(entity));
        return super.forInsertEntity(tableInfo, entity, ignoreNulls);
    }

    @Override
    public String forInsertEntityWithPk(TableInfo tableInfo, Object entity, boolean ignoreNulls) {
        beforeInsertCheck(tableInfo.getTableName(), List.of(entity));
        return super.forInsertEntityWithPk(tableInfo, entity, ignoreNulls);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String forInsertEntityBatch(TableInfo tableInfo, List<?> entities) {

        beforeInsertCheck(tableInfo.getTableName(), (List<Object>) entities);
        return super.forInsertEntityBatch(tableInfo, entities);
    }

    @Override
    public String forUpdateById(String schema, String tableName, Row row) {
        final Class<?> entityClass = CacheTableInfoUtils.nNRealTableInfo(tableName).getEntityClass();
        beforeUpdateCheck(tableName, List.of(RowUtil.toEntity(row, entityClass)));
        return super.forUpdateById(schema, tableName, row);
    }

    @Override
    public String forUpdateByQuery(QueryWrapper queryWrapper, Row row) {
        final List<QueryTable> queryTables = CPI.getQueryTables(queryWrapper);
        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(queryWrapper);


        queryTables.forEach(queryTable -> {
            final Class<?> entityClass = CacheTableInfoUtils.nNRealTableInfo(queryTable.getName()).getEntityClass();
            final List<Object> entities = List.of(RowUtil.toEntity(row, entityClass));
            beforeUpdateCheck(queryTable.getName(), entities);
            Optional.ofNullable(TABLE_CONFIG_MAP.get(queryTable.getNameWithSchema()))
                    .map(it -> it.get(OperateType.UPDATE))
                    .map(TableOneOperateConfig::getBefore)
                    .ifPresent(b -> b.accept(authContextSupplier.get(), new OperateInfo<>(whereQueryCondition, entities)));
        });
        return super.forUpdateByQuery(queryWrapper, row);
    }

    @Override
    public String forUpdateBatchById(String schema, String tableName, List<Row> rows) {
        final Class<?> entityClass = CacheTableInfoUtils.nNRealTableInfo(tableName).getEntityClass();
        beforeUpdateCheck(tableName, List.of(RowUtil.toEntityList(rows, entityClass)));
        return super.forUpdateBatchById(schema, tableName, rows);
    }

    @Override
    public String forUpdateEntity(TableInfo tableInfo, Object entity, boolean ignoreNulls) {
        beforeUpdateCheck(tableInfo.getTableName(), List.of(entity));
        return super.forUpdateEntity(tableInfo, entity, ignoreNulls);
    }

    @Override
    public String forUpdateEntityByQuery(TableInfo tableInfo, Object entity, boolean ignoreNulls,
                                         QueryWrapper queryWrapper) {

        final List<Object> entities = toUpdateEntity(tableInfo, queryWrapper).orElse(List.of(entity));
        beforeUpdateCheck(tableInfo.getTableName(), entities);

        final QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(tableInfo);
        Optional.ofNullable(TABLE_CONFIG_MAP.get(queryTable.getNameWithSchema()))
                .map(it -> it.get(OperateType.UPDATE))
                .map(TableOneOperateConfig::getBefore)
                .ifPresent(b -> b.accept(authContextSupplier.get(), new OperateInfo<>(CPI.getWhereQueryCondition(queryWrapper), entities)));

        return super.forUpdateEntityByQuery(tableInfo, entity, ignoreNulls, queryWrapper);
    }

    /**
     * use list because we may need to check all id in other table, but you don't
     * want load list's size times query
     *
     * @param tableName
     * @param entities
     */
    private void beforeInsertCheck(String tableName, List<Object> entities) {
        final AuthContext c = authContextSupplier.get();
        Optional.of(TABLE_CONFIG_MAP).map(map -> map.get(tableName))
                .map(map -> map.get(OperateType.INSERT))
                .map(TableOneOperateConfig::getChecking)
                .ifPresent(it -> it.accept(c, entities));

    }

    private void beforeUpdateCheck(String tableName, List<Object> entities) {
        Optional.of(TABLE_CONFIG_MAP).map(map -> map.get(tableName))
                .map(map -> map.get(OperateType.UPDATE))
                .map(TableOneOperateConfig::getChecking)
                .ifPresent(it -> it.accept(authContextSupplier.get(), entities));
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
}
