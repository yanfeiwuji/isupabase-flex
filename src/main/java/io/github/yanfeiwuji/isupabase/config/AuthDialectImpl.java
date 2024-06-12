package io.github.yanfeiwuji.isupabase.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ReferenceUtil;
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
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class AuthDialectImpl extends CommonsDialectImpl {
    private static Map<String, Map<OperateType, RlsPolicy<Object>>> rlsPolicyMap = Map.of();

    public AuthDialectImpl(KeywordWrap keywordWrap, LimitOffsetProcessor limitOffsetProcessor) {
        super(keywordWrap, limitOffsetProcessor);
    }

    @Override
    public void prepareAuth(QueryWrapper queryWrapper, OperateType operateType) {

        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(queryWrapper);
        applyRls(queryWrapper, operateType);
        conditions(whereQueryCondition, new ArrayList<>()).forEach(it -> {
            final QueryWrapper qw = it.getQueryWrapper();
            applyRls(qw, operateType);
        });

        super.prepareAuth(queryWrapper, operateType);
    }


    public List<OperatorSelectCondition> conditions(QueryCondition queryCondition, List<OperatorSelectCondition> selectConditions) {
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

        queryTables.forEach(it ->
                Optional.ofNullable(rlsPolicyMap).map(map -> map.get(it.getName()))
                        .map(map -> map.get(operateType))
                        .map(RlsPolicy::using)
                        .map(Supplier::get)

                        .ifPresent(queryWrapper::and)
        );
    }

    // todo change  to config chain
    public static synchronized void loadRls(List<RlsPolicyFor> rlsPolicies) {
        AuthDialectImpl.rlsPolicyMap = rlsPolicies.stream().collect(Collectors.groupingBy(RlsPolicyFor::tableName,
                Collectors.mapping(it -> it,
                        Collectors.toMap(RlsPolicyFor::operateType, RlsPolicyFor::rlsPolicy)
                )
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

        queryTables.stream().map(QueryTable::getName)
                .forEach(tableName -> {
                    final Class<?> entityClass = CacheTableInfoUtils.nNRealTableInfo(tableName).getEntityClass();
                    beforeUpdateCheck(tableName, List.of(RowUtil.toEntity(row, entityClass)));
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
    public String forUpdateEntityByQuery(TableInfo tableInfo, Object entity, boolean ignoreNulls, QueryWrapper queryWrapper) {

        final List<Object> objects = toUpdateEntity(tableInfo, queryWrapper).orElse(List.of(entity));
        beforeUpdateCheck(tableInfo.getTableName(), objects);
        return super.forUpdateEntityByQuery(tableInfo, entity, ignoreNulls, queryWrapper);
    }


    /**
     * use list because we may need to check all id in other table, but you don't want load list's size times query
     *
     * @param tableName
     * @param entities
     */
    private void beforeInsertCheck(String tableName, List<Object> entities) {
        Optional.ofNullable(rlsPolicyMap).map(map -> map.get(tableName))
                .map(map -> map.get(OperateType.INSERT))
                .map(RlsPolicy::check)
                .ifPresent(it -> it.accept(entities));
    }

    private void beforeUpdateCheck(String tableName, List<Object> entities) {
        Optional.ofNullable(rlsPolicyMap).map(map -> map.get(tableName))
                .map(map -> map.get(OperateType.UPDATE))
                .map(RlsPolicy::check)
                .ifPresent(it -> it.accept(entities));
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
