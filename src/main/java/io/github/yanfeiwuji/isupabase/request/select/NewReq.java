package io.github.yanfeiwuji.isupabase.request.select;

import java.util.List;
import java.util.Objects;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.relation.ManyToOne;
import com.mybatisflex.core.relation.OneToOne;

import io.github.yanfeiwuji.isupabase.request.utils.RelationUtils;

import lombok.Data;

@Data
public class NewReq {

    AbstractRelation<?> relation; // sub

    QueryTable queryTable;
    List<QueryColumn> queryColumns = List.of();

    // use in roole
    @SuppressWarnings("rawtypes")
    List<AbstractRelation> joinRelations = List.of();

    List<QueryOrderBy> orders = List.of();
    QueryCondition queryCondition = QueryCondition.createEmpty();

    // up use
    boolean inner = false;

    List<NewReq> subs = List.of();

    Number limit;
    Number offset;

    public QueryWrapper handler(QueryWrapper queryWrapper) {
        select(queryWrapper);
        from(queryWrapper);
        inner(queryWrapper);
        join(queryWrapper);
        condition(queryWrapper);
        order(queryWrapper);
        range(queryWrapper);
        return queryWrapper;
    }

    private void select(QueryWrapper queryWrapper) {
        queryWrapper.select(queryColumns);
    }

    private void from(QueryWrapper queryWrapper) {
        if (Objects.isNull(relation)) {
            queryWrapper.from(queryTable);
        }
    }

    private void inner(QueryWrapper queryWrapper) {
        this.subs.stream().filter(it -> it.isInner())
                .forEach(innerSubs -> {
                    queryWrapper.and(QueryMethods.exists(RelationUtils
                            .relationExistQueryWrapper(innerSubs.relation)
                            .and(queryCondition))
                            .and(innerSubs.queryCondition));
                });
    }

    private void condition(QueryWrapper queryWrapper) {
        queryWrapper.and(queryCondition);
    }

    private void join(QueryWrapper queryWrapper) {
        joinRelations.stream()
                .filter(it -> it instanceof OneToOne || it instanceof ManyToOne)
                .forEach(it -> RelationUtils.relationJoin(queryWrapper, it));
    }

    private void order(QueryWrapper queryWrapper) {
        orders.forEach(queryWrapper::orderBy);
    }

    private void range(QueryWrapper queryWrapper) {
        queryWrapper.limit(limit);
        queryWrapper.offset(offset);
    }
}
