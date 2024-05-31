package io.github.yanfeiwuji.isupabase.request.select;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.util.StringUtil;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

import static com.mybatisflex.core.query.QueryMethods.column;

@UtilityClass
public class QueryExecInvoke {

    private record TargetValues(Set<Object> targetValues, List<Row> mappingRows) {

    }

    public List<?> invoke(QueryExec queryExec, BaseMapper<?> baseMapper) {
        // // only from root
        // if (Objects.nonNull(queryExec.getRelation())) {
        // return List.of();
        // }
        // QueryWrapper queryWrapper = queryExec.handler(QueryWrapper.create());
        // List<?> preList = baseMapper.selectListByQuery(queryWrapper);
        // Optional.ofNullable(queryExec.getSubs()).orElse(List.of()).parallelStream().forEach(exec
        // -> embeddedList(exec, baseMapper, preList));




      return embeddedList(queryExec, baseMapper, null);

    }

    @SuppressWarnings("rawtypes")
    private List<?> embeddedList(QueryExec queryExec, BaseMapper<?> baseMapper, List preList) {
        List<?> targetObjectList;
        if (Objects.isNull(queryExec.getRelation())) {
            QueryWrapper queryWrapper = queryExec.handler(QueryWrapper.create());
            targetObjectList = baseMapper.selectListByQuery(queryWrapper);
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
            Class<?> clazz = relation.isOnlyQueryValueField() ? relation.getTargetEntityClass()
                    : relation.getMappingType();
            targetObjectList = baseMapper.selectListByQueryAs(queryWrapper, clazz);
            relation.join(preList, targetObjectList, targetValues.mappingRows());

        }
        List<?> finalTargetObjectList = targetObjectList;
        Optional.ofNullable(queryExec.getSubs()).orElse(List.of()).parallelStream()
                .forEach(exec -> embeddedList(exec, baseMapper, finalTargetObjectList));
        QueryExecInvoke.removeProperties(queryExec, targetObjectList);
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

    @SuppressWarnings("rawtypes")
    private TargetValues embeddedTargetValues(AbstractRelation relation, List preList) {
        return new TargetValues(relation.getSelfFieldValues(preList), null);
    }

    @SuppressWarnings("rawtypes")
    private TargetValues embeddedJoinTargetValues(BaseMapper<?> baseMapper, AbstractRelation relation, List preList) {
        Set selfFieldValues = relation.getSelfFieldValues(preList);
        if (selfFieldValues.isEmpty()) {
            return new TargetValues(Set.of(), List.of());
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(column(relation.getJoinSelfColumn()), column(relation.getJoinTargetColumn()))
                .from(relation.getJoinTable());
        if (selfFieldValues.size() > 1) {
            queryWrapper.where(column(relation.getJoinSelfColumn()).in(selfFieldValues));
        } else {
            queryWrapper.where(column(relation.getJoinSelfColumn()).eq(selfFieldValues.iterator().next()));
        }
        List<Row> mappingRows = baseMapper.selectListByQueryAs(queryWrapper, Row.class);

        Set<Object> targetValues = mappingRows
                .stream()
                .map(it -> it.getIgnoreCase(relation.getJoinTargetColumn()))
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        return new TargetValues(targetValues, mappingRows);
    }

    private void removeProperties(QueryExec queryExec, List<?> targetObjectList) {

        targetObjectList.parallelStream().forEach(obj -> Optional.ofNullable(
                queryExec.getNeedRemoves()).orElse(List.of()).parallelStream().forEach(it -> it.set(null, obj)));
    }

}
