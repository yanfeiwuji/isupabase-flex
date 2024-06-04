package io.github.yanfeiwuji.isupabase.request.select;

import cn.hutool.core.util.StrUtil;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.util.StringUtil;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.RelationUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.mybatisflex.core.query.QueryMethods.column;

@UtilityClass
public class QueryExecInvoke {

    private static final Logger log = LoggerFactory.getLogger(QueryExecInvoke.class);

    private record TargetValues(Set<Object> targetValues, List<Row> mappingRows) {
    }

    public String filter() {
        return "";
    }

    public List<?> invoke(QueryExec queryExec, BaseMapper<?> baseMapper) {
        return embeddedList(queryExec, baseMapper, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Map> embeddedList(QueryExec queryExec, BaseMapper<?> baseMapper, List preList) {
        List<Map> targetObjectList;
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
                return preList;
            }

            QueryWrapper queryWrapper = relation.buildQueryWrapper(targetValues.targetValues());
            queryExec.handler(queryWrapper);

            targetObjectList = baseMapper.selectListByQueryAs(queryWrapper, Map.class);


            RelationUtils.join(relation, preList, targetObjectList, targetValues.mappingRows, false);

        }
        List<?> finalTargetObjectList = targetObjectList;
        Optional.ofNullable(queryExec.getSubs())
                .orElse(List.of())
                .parallelStream()
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
            queryWrapper.where(CacheTableInfoUtils.nNRelJoinSelfQueryColumn(relation).eq(selfFieldValues.iterator().next()));
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
    private void modifyKeys(QueryExec queryExec, List<Map> targetObjectList) {
        long startTime = System.currentTimeMillis();
        log.info("start:{}", startTime);
        targetObjectList.parallelStream().forEach(item -> {
            if (Objects.isNull(item)) {
                return;
            }

            // remove
            queryExec.getPickKeys().forEach(key -> item.putIfAbsent(key, null));
            final List<String> removeKeys = item.keySet().stream()
                    .filter(key -> !queryExec.getPickKeys().contains(key))
                    .toList();
            removeKeys.forEach(item::remove);
            // cast
            for (Map.Entry<String, String> entry : Optional.ofNullable(queryExec.getCastMap()).orElse(Map.of()).entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                try {
                    Object needValue = ValueUtils.cast(value, item.get(key));
                    item.replace(key, needValue);
                } catch (RuntimeException e) {
                    throw PgrstExFactory.exCasingError(e.getMessage()).get();
                }
            }
            // rename
            Optional.ofNullable(queryExec.getRenameMap()).orElse(Map.of()).forEach((k, v) -> {
                final Object temp = item.get(k);
                item.remove(k);
                item.put(v, temp);
            });

        });
        log.info("modify all :{}", System.currentTimeMillis() - startTime);

    }

}
