package io.github.yanfeiwuji.isupabase.request.select;

import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.relation.ManyToOne;
import com.mybatisflex.core.relation.OneToOne;

import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.util.FieldWrapper;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.RelationUtils;

import lombok.Data;

@Data
public class QueryExec {

    private AbstractRelation<?> relation; // sub

    private String relEnd;

    private QueryTable queryTable;

    private TableInfo tableInfo;

    private List<QueryColumn> queryColumns;

    // use in order
    private List<AbstractRelation<?>> joins;

    private List<QueryOrderBy> orders;

    private QueryCondition queryCondition = QueryCondition.createEmpty();

    private List<QueryExec> subs;

    // up use
    private boolean inner = false;
    private Number limit;
    private Number offset;

    private Map<String, AbstractRelation<?>> subRelMap;

    private boolean all;
    private List<FieldWrapper> needRemoves;


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

    private QueryWrapper createQueryWrapper() {

        return QueryWrapper.create();
    }

    private void select(QueryWrapper queryWrapper) {

        if (Objects.isNull(queryColumns)) {
            return;
        }
        if (all) {
            queryWrapper.select(queryColumns);
        } else {
            Map<String, QueryColumn> map =
                    queryColumns.stream().collect(Collectors.toMap(QueryColumn::getName, qc -> qc));
            // handler sub
            Optional.ofNullable(this.getSubs()).orElse(List.of())
                    .stream().map(QueryExec::getRelation)
                    .forEach(rel -> {
                        QueryColumn queryColumn = CacheTableInfoUtils.nNRelSelfQueryColumn(rel);
                        if (!map.containsKey(queryColumn.getName())) {
                            map.put(queryColumn.getName(), queryColumn);
                            this.addNeedRemove(rel.getSelfFieldWrapper());
                        }
                    });
            // handler queryinfo
            if (Objects.nonNull(relation)) {
                QueryColumn queryColumn = CacheTableInfoUtils.nNRelTargetQueryColumn(this.relation);
                if (!map.containsKey(queryColumn.getName())) {
                    map.put(queryColumn.getName(), queryColumn);
                    this.addNeedRemove(relation.getTargetFieldWrapper());
                }
            }
            queryWrapper.select(map.values().toArray(new QueryColumn[0]));
        }


    }

    private void from(QueryWrapper queryWrapper) {
        if (Objects.isNull(relation)) {
            queryWrapper.from(queryTable);
        }
    }

    private void inner(QueryWrapper queryWrapper) {
        Optional.ofNullable(this.subs)
                .orElse(List.of())
                .stream().filter(QueryExec::isInner)
                .forEach(innerSubs -> queryWrapper.and(QueryMethods.exists(
                                RelationUtils
                                        .relationExistQueryWrapper(innerSubs.relation)
                                        .and(innerSubs.queryCondition)
                        )
                ));
    }

    private void condition(QueryWrapper queryWrapper) {
        queryWrapper.and(queryCondition);
    }

    private void join(QueryWrapper queryWrapper) {
        Optional.ofNullable(joins)
                .orElse(List.of())
                .stream()
                .filter(it -> it instanceof OneToOne || it instanceof ManyToOne)
                .forEach(it -> RelationUtils.relationJoin(queryWrapper, it));
    }

    private void order(QueryWrapper queryWrapper) {
        Optional.ofNullable(orders)
                .orElse(List.of())
                .forEach(queryWrapper::orderBy);
    }

    private void range(QueryWrapper queryWrapper) {
        queryWrapper.limit(limit);
        queryWrapper.offset(offset);
    }

    public void addQueryColumn(QueryColumn queryColumn) {
        if (queryColumns == null) {
            queryColumns = new ArrayList<>();
        }
        if (all) {
            return;
        }

        if (CommonStr.STAR.equals(queryColumn.getName())) {
            queryColumns = List.of(queryColumn);
            this.all = true;
        } else {
            queryColumns.add(queryColumn);
        }
    }

    @SuppressWarnings("rawtypes")
    public void addJoin(AbstractRelation relation) {
        if (joins == null) {
            joins = new ArrayList<>();
        }
        joins.add(relation);
    }

    public void addOrder(QueryOrderBy order) {
        if (orders == null) {
            orders = new ArrayList<>();
        }
        orders.add(order);
    }

    public void addSub(QueryExec sub) {
        if (subs == null) {
            subs = new ArrayList<>();
        }
        subs.add(sub);
    }

    public void putSubRelMap(String key, AbstractRelation<?> relation) {
        if (subRelMap == null) {
            subRelMap = new HashMap<>();
        }
        subRelMap.put(key, relation);
    }

    public void addNeedRemove(FieldWrapper wrapper) {
        if (needRemoves == null) {
            needRemoves = new ArrayList<>();
        }
        needRemoves.add(wrapper);
    }
}
