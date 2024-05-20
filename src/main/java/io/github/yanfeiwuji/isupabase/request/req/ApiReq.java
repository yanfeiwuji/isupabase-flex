package io.github.yanfeiwuji.isupabase.request.req;

import java.util.List;
import java.util.Objects;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import cn.hutool.json.JSONUtil;
import io.github.yanfeiwuji.isupabase.request.filter.Filter;
import io.github.yanfeiwuji.isupabase.request.select.Select;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

public record ApiReq(Select select, List<Filter> filters, List<Filter> subFilter) {

    public void handler(QueryChain<?> queryChain) {

        queryChain.select(select.columnsWithSub());
        handlerJoin(queryChain);
        // .leftJoin(Object.class).on("null")
        queryChain.where(
                filters.stream().map(it -> it.toQueryCondition()).reduce(QueryCondition::and)
                        .orElse(QueryCondition.createEmpty()));

    }

    public void handlerJoin(QueryChain<?> queryChain) {
        List<AbstractRelation<?>> abstractRelations = select.abstractRelations();
        abstractRelations.forEach(it -> {

            it.getSelfEntityClass();
            it.getTargetEntityClass();

            it.getJoinTable();
            it.getJoinSelfColumn();
            it.getJoinTargetColumn();

            TableInfo tableInfo = TableInfoFactory.ofTableName(it.getJoinTable());

            TableInfo tableInfo2 = TableInfoFactory.ofEntityClass(it.getSelfEntityClass());
            TableInfo targetTable = TableInfoFactory.ofEntityClass(it.getTargetEntityClass());

            if (Objects.nonNull(tableInfo)) {
                // it.getJoinTable();
                ;

                queryChain.leftJoin(tableInfo.getEntityClass())
                        .on(QueryCondition.create(null, abstractRelations));
            } else {

                CacheTableInfoUtils.realQueryColumn(null, tableInfo2);
                CacheTableInfoUtils.realQueryColumn("", tableInfo2);
                queryChain.leftJoin(it.getTargetEntityClass());

            }
            System.out.println(it.getJoinTable());
            System.out.println(JSONUtil.toJsonStr(it));
        });

    }

}
