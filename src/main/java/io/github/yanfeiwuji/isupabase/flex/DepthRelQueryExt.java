package io.github.yanfeiwuji.isupabase.flex;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

import java.util.List;
import java.util.stream.Collectors;

public record DepthRelQueryExt(List<QueryColumn> selects, QueryCondition condition) {

    public List<QueryColumn> addTargetColumn(AbstractRelation<?> relation) {
        boolean hasAll = selects.stream().anyMatch(it -> it.getName().equals(CommonStr.STAR));
        TableInfo tableInfo = TableInfoFactory.ofEntityClass(relation.getTargetEntityClass());
        if (hasAll) {
            return List.of(CacheTableInfoUtils.nNQueryAllColumns(tableInfo));
        } else {
            QueryColumn queryColumn = CacheTableInfoUtils.nNRelTargetQueryColumn(relation);
            List<QueryColumn> next = selects.stream().collect(Collectors.toList());
            next.add(queryColumn);
            return next;
        }
    }

    public boolean needToClearTargetColumn(AbstractRelation<?> relation) {
        String column = CacheTableInfoUtils.nNRelTargetQueryColumn(relation).getName();
        return selects.stream().map(it -> it.getName()).noneMatch(it -> it.equals(column));
    }

}
