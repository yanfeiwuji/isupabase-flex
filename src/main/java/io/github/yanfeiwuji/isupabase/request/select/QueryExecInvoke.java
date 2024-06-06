package io.github.yanfeiwuji.isupabase.request.select;

import cn.hutool.core.util.StrUtil;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.util.StringUtil;
import io.github.yanfeiwuji.isupabase.config.TrackExecutionTime;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.RelationUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

import static com.mybatisflex.core.query.QueryMethods.column;

@UtilityClass
public class QueryExecInvoke {

    private record TargetValues(Set<Object> targetValues, List<Row> mappingRows) {
    }


    @SuppressWarnings({"rawtyes", "uncheck"})
    public List<?> invoke(QueryExec queryExec, BaseMapper<?> baseMapper) {
        return embeddedList(queryExec, baseMapper, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Map> embeddedList(QueryExec queryExec, BaseMapper<?> baseMapper, List preList) {
        List<Map> targetObjectList = preList;
        if (Objects.isNull(queryExec.getRelation())) {
            QueryWrapper queryWrapper = queryExec.handler(QueryWrapper.create());
            targetObjectList = baseMapper.selectListByQueryAs(queryWrapper, Map.class);
        } else {
            AbstractRelation<?> relation = queryExec.getRelation();
            choiceDs(relation);
            TargetValues targetValues = Optional.of(relation.getJoinTable()).filter(StrUtil::isNotBlank)
                    .map(it -> embeddedJoinTargetValues(baseMapper, relation, preList))
                    .orElseGet(() -> embeddedTargetValues(relation, preList));

            if (targetValues.targetValues().isEmpty()) {
                // handler prelist join a empty
                return preList;
            }else {

                QueryWrapper queryWrapper = relation.buildQueryWrapper(targetValues.targetValues());
                queryExec.handler(queryWrapper);

                targetObjectList = baseMapper.selectListByQueryAs(queryWrapper, Map.class);

                RelationUtils.join(relation, preList, targetObjectList, targetValues.mappingRows, queryExec.isSpread());

            }


        }


        List<?> finalTargetObjectList = targetObjectList;
        Optional.ofNullable(queryExec.getSubs())
                .orElse(List.of())
                .parallelStream()
                .filter(it -> !it.isNotExec())
                .forEach(exec -> embeddedList(exec, baseMapper, finalTargetObjectList));

        modifyKeys(queryExec, targetObjectList);
        return targetObjectList;
    }

    private void choiceDs(AbstractRelation<?> relation) {
        String currentDsKey = DataSourceKey.get();
        String configDsKey = relation.getDataSource();
        if (StringUtil.isBlank(configDsKey) && currentDsKey != null) {
            configDsKey = currentDsKey;
        }
        if (StringUtil.isNotBlank(configDsKey)) {
            DataSourceKey.use(configDsKey);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private TargetValues embeddedTargetValues(AbstractRelation relation, List preList) {
        return new TargetValues(RelationUtils.selfFieldValues(relation, preList), null);
    }

    @SuppressWarnings("rawtypes")
    private TargetValues embeddedJoinTargetValues(BaseMapper<?> baseMapper, AbstractRelation relation, List preList) {
        @SuppressWarnings("unchecked")

        Set selfFieldValues = RelationUtils.selfFieldValues(relation, preList);

        if (selfFieldValues.isEmpty()) {
            return new TargetValues(Set.of(), List.of());
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(column(relation.getJoinSelfColumn()), column(relation.getJoinTargetColumn()))
                .from(relation.getJoinTable());
        if (selfFieldValues.size() > 1) {

            queryWrapper.where(CacheTableInfoUtils.nNRelJoinSelfQueryColumn(relation).in(selfFieldValues));
        } else {
            queryWrapper.where(
                    CacheTableInfoUtils.nNRelJoinSelfQueryColumn(relation).eq(selfFieldValues.iterator().next()));
        }
        List<Row> mappingRows = baseMapper.selectListByQueryAs(queryWrapper, Row.class);

        Set<Object> targetValues = mappingRows
                .stream()
                .map(it -> it.getIgnoreCase(relation.getJoinTargetColumn()))
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        return new TargetValues(targetValues, mappingRows);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @TrackExecutionTime
    private void modifyKeys(QueryExec queryExec, List<Map> targetObjectList) {

        final Map<String, String> pickKeys = Optional.ofNullable(queryExec.getPickKeyMap()).orElse(Map.of());
        final Map<String, String> castMap = Optional.ofNullable(queryExec.getCastMap()).orElse(Map.of());
        final Map<String, String> renameMap = Optional.ofNullable(queryExec.getRenameMap()).orElse(Map.of());

        targetObjectList.forEach(item -> {
            if (Objects.isNull(item)) {
                return;
            }
            pickKeys.forEach((k, v) -> item.putIfAbsent(k, null));
            final List<String> removeKeys = item.keySet().stream()
                    .map(Object::toString)
                    .filter(k -> !pickKeys.containsKey(k))
                    .toList();

            removeKeys.forEach(item::remove);

            try {
                castMap.forEach((k, v) -> {
                    Object needValue = ValueUtils.cast(v, item.get(k));
                    item.replace(k, needValue);
                });
            } catch (RuntimeException e) {
                throw PgrstExFactory.exCasingError(e.getMessage()).get();
            }

            renameMap.forEach((k, v) -> {
                final Object temp = item.get(k);
                item.remove(k);
                item.put(v, temp);
            });
        });
    }

}
