package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.relation.ToManyRelation;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class RelationUtils {

    public QueryWrapper relationExistQueryWrapper(AbstractRelation<?> relation) {

        String joinTable = relation.getJoinTable();
        if (CharSequenceUtil.isNotBlank(joinTable)) {
            return relationExistJoin(relation);
        } else {
            return relationExistNoJoin(relation);
        }
    }

    private QueryWrapper relationExistJoin(AbstractRelation<?> relation) {
        //  QueryWrapper queryWrapper = QueryWrapper.create();
        TableInfo targetTableInfo = relation.getTargetTableInfo();

        String joinTable = relation.getJoinTable();
        TableInfo joinTableInfo = TableInfoFactory.ofTableName(joinTable);
        QueryTable joinQueryTable = CacheTableInfoUtils.nNQueryTable(joinTableInfo);

        QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(targetTableInfo);

        QueryColumn targetColumn = CacheTableInfoUtils.nNRelTargetQueryColumn(relation);
        QueryColumn selfColumn = CacheTableInfoUtils.nNRelSelfQueryColumn(relation);

        QueryColumn joinTargetQueryColumn = CacheTableInfoUtils.nNRelJoinTargetQueryColumn(relation);
        QueryColumn joinSelfQueryColumn = CacheTableInfoUtils.nNRelJoinSelfQueryColumn(relation);
        return QueryMethods.selectOne().from(queryTable)
                .join(joinQueryTable).on(targetColumn.eq(joinTargetQueryColumn))
                .where(joinSelfQueryColumn.eq(selfColumn));
    }

    private QueryWrapper relationExistNoJoin(AbstractRelation<?> relation) {
        TableInfo subTableInfo = relation.getTargetTableInfo();
        QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(subTableInfo);
        QueryColumn targetColumn = CacheTableInfoUtils.nNRelTargetQueryColumn(relation);
        QueryColumn selfColumn = CacheTableInfoUtils.nNRelSelfQueryColumn(relation);
        return QueryMethods.selectOne()
                .from(queryTable)
                .where(targetColumn.eq(selfColumn));

    }

    public void relationJoin(QueryWrapper queryWrapper, AbstractRelation<?> relation) {
        String joinTable = relation.getJoinTable();
        if (CharSequenceUtil.isNotBlank(joinTable)) {
            addJoinHasJoin(queryWrapper, relation);
        } else {
            addJoinNoJoin(queryWrapper, relation);
        }
    }

    private void addJoinHasJoin(QueryWrapper queryWrapper, AbstractRelation<?> relation) {
        TableInfo targetTableInfo = relation.getTargetTableInfo();
        String joinTable = relation.getJoinTable();

        TableInfo joinTableInfo = TableInfoFactory.ofTableName(joinTable);

        QueryTable joinQueryTable = CacheTableInfoUtils.nNQueryTable(joinTableInfo);

        QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(targetTableInfo);

        QueryColumn targetColumn = CacheTableInfoUtils.nNRelTargetQueryColumn(relation);
        QueryColumn joinSelfQueryColumn = CacheTableInfoUtils.nNRelJoinSelfQueryColumn(relation);

        QueryColumn joinTargetQueryColumn = CacheTableInfoUtils.nNRelJoinTargetQueryColumn(relation);
        QueryColumn selfQueryColumn = CacheTableInfoUtils.nNRelSelfQueryColumn(relation);

        queryWrapper.join(joinQueryTable)
                .on(joinSelfQueryColumn.eq(selfQueryColumn))
                .join(queryTable).on(joinTargetQueryColumn.eq(targetColumn));

    }

    private void addJoinNoJoin(QueryWrapper queryWrapper, AbstractRelation<?> relation) {

        TableInfo targetTableInfo = relation.getTargetTableInfo();
        QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(targetTableInfo);

        QueryColumn selfQueryColumn = CacheTableInfoUtils.nNRelSelfQueryColumn(relation);
        QueryColumn targetQueryColumn = CacheTableInfoUtils.nNRelTargetQueryColumn(relation);

        queryWrapper.leftJoin(queryTable).on(targetQueryColumn.eq(selfQueryColumn));
    }

    public Set<Object> selfFieldValues(AbstractRelation<?> relation, List<Map<String, Object>> preList) {
        final String column = selfFieldColumn(relation);
        return preList.stream().map(it -> it.get(column))
                .filter(Objects::nonNull)
                .filter(it -> !CharSequenceUtil.EMPTY.equals(it))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("rawtypes")
    public void join(AbstractRelation<?> relation,
                     List<Map> selfEntities,
                     List<Map> targetObjectList,
                     List<Row> mappingRows,
                     boolean spread) {

        if (relation instanceof ToManyRelation<?>) {
            joinMany(relation, selfEntities, targetObjectList, mappingRows);
        } else {
            joinOne(relation, selfEntities, targetObjectList, mappingRows, spread);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void joinOne(AbstractRelation<?> relation, List<Map> selfEntities, List<Map> targetObjectList,
                         List<Row> mappingRows, boolean spread) {
        final String selfFieldColumn = selfFieldColumn(relation);
        final String joinSelfColumn = relation.getJoinSelfColumn();
        final String joinTargetColumn = relation.getJoinTargetColumn();

        final String targetedFieldColumn = targetFieldColumn(relation);
        final String relationFieldName = relationFieldName(relation);
        final String valueField = relation.getValueField();

        selfEntities.forEach(selfEntity -> {
            Object selfValue = selfEntity.get(selfFieldColumn);

            if (selfValue != null) {
                selfValue = selfValue.toString();
                String targetMappingValue = null;
                if (mappingRows != null) {
                    Object finalSelfValue = selfValue;
                    targetMappingValue = mappingRows.stream()
                            .filter(it -> finalSelfValue.equals(it.getIgnoreCase(joinSelfColumn)))
                            .map(it -> it.getIgnoreCase(joinTargetColumn))
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .findFirst().orElse(null);

                    if (targetMappingValue == null) {
                        return;
                    }
                } else {
                    targetMappingValue = (String) selfValue;
                }
                for (Map<String, Object> targetObject : targetObjectList) {
                    Object targetValue = targetObject.get(targetedFieldColumn);
                    if (targetValue != null && targetMappingValue.equals(targetValue.toString())) {
                        if (relation.isOnlyQueryValueField()) {
                            selfEntity.put(relationFieldName, targetObject.get(valueField));
                        } else {
                            if (spread) {
                                selfEntity.putAll(targetObject);
                            } else {
                                selfEntity.put(relationFieldName, targetObject);
                            }
                        }
                        break;
                    }
                }
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void joinMany(AbstractRelation<?> relation, List<Map> selfEntities, List<Map> targetObjectList,
                          List<Row> mappingRows) {

        final String selfFieldColumn = selfFieldColumn(relation);
        final String joinSelfColumn = relation.getJoinSelfColumn();
        final String joinTargetColumn = relation.getJoinTargetColumn();

        final String targetedFieldColumn = targetFieldColumn(relation);
        final String relationFieldName = relationFieldName(relation);
        final String valueField = relation.getValueField();
        final String manyRelSelfValueSplitBy = CacheTableInfoUtils.endManyRelSelfValueSplitBy(relation)
                .orElse(CharSequenceUtil.EMPTY);

        // 目标表关联字段->目标表对象
        Map<String, List<Map<String, Object>>> leftFieldToRightTableMap = new HashMap<>(targetObjectList.size());
        for (Map targetObject : targetObjectList) {
            Object targetJoinFieldValue = targetObject.get(targetedFieldColumn);
            if (targetJoinFieldValue != null) {
                leftFieldToRightTableMap.computeIfAbsent(targetJoinFieldValue.toString(), k -> new ArrayList<>(1))
                        .add(targetObject);
            }
        }

        // 通过中间表
        if (mappingRows != null) {
            // 当使用中间表时，需要重新映射关联关系
            Map<String, List<Map<String, Object>>> temp = new HashMap<>(selfEntities.size());
            for (Row mappingRow : mappingRows) {
                Object midTableJoinSelfValue = mappingRow.getIgnoreCase(joinSelfColumn);
                if (midTableJoinSelfValue == null) {
                    continue;
                }
                Object midTableJoinTargetValue = mappingRow.getIgnoreCase(joinTargetColumn);
                if (midTableJoinTargetValue == null) {
                    continue;
                }
                List<Map<String, Object>> targetObjects = leftFieldToRightTableMap
                        .get(midTableJoinTargetValue.toString());

                if (targetObjects == null) {
                    continue;
                }
                temp.computeIfAbsent(midTableJoinSelfValue.toString(), k -> new ArrayList<>(targetObjects.size()))
                        .addAll(targetObjects);
            }
            leftFieldToRightTableMap = temp;
        }

        for (Map selfEntity : selfEntities) {
            if (selfEntity == null) {
                continue;
            }
            Object selfValue = selfEntity.get(selfFieldColumn);
            if (selfValue == null) {
                continue;
            }
            selfValue = selfValue.toString();

            // 只有当splitBy不为空时才会有多个值
            Set<String> targetMappingValues;

            if (CharSequenceUtil.isNotBlank(manyRelSelfValueSplitBy)) {
                String[] splitValues = ((String) selfValue).split(manyRelSelfValueSplitBy);
                targetMappingValues = new LinkedHashSet<>(Arrays.asList(splitValues));
            } else {
                targetMappingValues = new HashSet<>(1);
                targetMappingValues.add((String) selfValue);
            }
            final List<Map<String, Object>> putList;


            if (targetMappingValues.isEmpty()) {
                putList = List.of();
            } else {
                Map<String, List<Map<String, Object>>> finalLeftFieldToRightTableMap = leftFieldToRightTableMap;

                putList = targetMappingValues.stream()
                        .flatMap(it -> Optional.ofNullable(finalLeftFieldToRightTableMap.get(it))
                                .orElse(List.of()).stream())
                        .toList();
            }
            System.out.println(putList.size() + "==dsfs");
            if (relation.isOnlyQueryValueField()) {
                selfEntity.put(relationFieldName, putList.stream().map(it -> it.get(valueField)).toList());
            } else {
                selfEntity.put(relationFieldName, putList);
            }
        }
    }

    private String selfFieldColumn(AbstractRelation<?> relation) {
        final TableInfo tableInfo = TableInfoFactory.ofEntityClass(relation.getSelfEntityClass());
        return tableInfo.getPropertyColumnMapping().get(relation.getSelfField().getName());
    }

    private String targetFieldColumn(AbstractRelation<?> relation) {
        final TableInfo tableInfo = TableInfoFactory.ofEntityClass(relation.getTargetEntityClass());
        return tableInfo.getPropertyColumnMapping().get(relation.getTargetField().getName());
    }

    private String relationFieldName(AbstractRelation<?> relation) {

        final String name = relation.getRelationField().getName();
        return CacheTableInfoUtils.propertyToParamKey(name);
    }

}
