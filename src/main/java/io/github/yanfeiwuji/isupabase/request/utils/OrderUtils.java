package io.github.yanfeiwuji.isupabase.request.utils;

import java.util.List;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import io.github.yanfeiwuji.isupabase.request.order.Order;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderUtils {

    public void apply(QueryWrapper queryWrapper, List<Order> orders) {

        // orders.stream()
        // .filter(null)
        // .map(Order::getJoin).forEach(rel -> {
        // if (CharSequenceUtil.isNotEmpty(rel.getJoinTable())) {
        // relationExistJoin(queryWrapper, rel);
        // } else {
        // relationNotExistJoin(queryWrapper, rel);
        // }
        // });
        // orders.stream().map(Order::getOrder).forEach(queryWrapper::orderBy);
    }

    private void relationExistJoin(QueryWrapper queryWrapper, AbstractRelation<?> relation) {

        TableInfo targetTableInfo = relation.getTargetTableInfo();

        String joinTable = relation.getJoinTable();
        TableInfo joinTableInfo = TableInfoFactory.ofTableName(joinTable);
        QueryTable joinQueryTable = CacheTableInfoUtils.nNQueryTable(joinTableInfo);

        QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(targetTableInfo);

        QueryColumn targetColumn = CacheTableInfoUtils.nNRelTargetQueryColumn(relation);
        QueryColumn selfColumn = CacheTableInfoUtils.nNRelSelfQueryColumn(relation);

        QueryColumn joinTargetQueryColumn = CacheTableInfoUtils.nNRelJoinTargetQueryColumn(relation);
        QueryColumn joinSelfQueryColumn = CacheTableInfoUtils.nNRelJoinSelfQueryColumn(relation);

        queryWrapper.join(joinQueryTable).on(targetColumn.eq(joinTargetQueryColumn))
                .join(queryTable).on(joinSelfQueryColumn.eq(
                        selfColumn));

    }

    private void relationNotExistJoin(QueryWrapper queryWrapper, AbstractRelation relation) {

        TableInfo targetTableInfo = relation.getTargetTableInfo();

        QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(targetTableInfo);

        QueryColumn selfColumn = CacheTableInfoUtils.nNRelSelfQueryColumn(relation);

        QueryColumn joinSelfQueryColumn = CacheTableInfoUtils.nNRelJoinSelfQueryColumn(relation);

        queryWrapper.join(queryTable).on(joinSelfQueryColumn.eq(selfColumn));

    }
}
