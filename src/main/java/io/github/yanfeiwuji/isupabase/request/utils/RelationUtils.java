package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import lombok.experimental.UtilityClass;

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
        QueryWrapper queryWrapper = QueryWrapper.create();
        TableInfo targetTableInfo = relation.getTargetTableInfo();

        String joinTable = relation.getJoinTable();
        TableInfo joinTableInfo = TableInfoFactory.ofTableName(joinTable);
        QueryTable joinQueryTable = CacheTableInfoUtils.nNQueryTable(joinTableInfo);

        QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(targetTableInfo);

        QueryColumn targetColumn = CacheTableInfoUtils.nNRelTargetQueryColumn(relation);
        QueryColumn selfColumn = CacheTableInfoUtils.nNRelSelfQueryColumn(relation);

        QueryColumn joinTargetQueryColumn = CacheTableInfoUtils.nNRelJoinTargetQueryColumn(relation);
        QueryColumn joinSelfQueryColumn = CacheTableInfoUtils.nNRelJoinSelfQueryColumn(relation);

        queryWrapper.select(CommonStr.ONE)
                .from(queryTable)
                .join(joinQueryTable).on(targetColumn.eq(joinTargetQueryColumn))
                .where(joinSelfQueryColumn.eq(selfColumn));

        return queryWrapper;
    }

    private QueryWrapper relationExistNoJoin(AbstractRelation<?> relation) {
        TableInfo subTableInfo = relation.getTargetTableInfo();
        QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(subTableInfo);
        QueryColumn targetColumn = CacheTableInfoUtils.nNRelTargetQueryColumn(relation);
        QueryColumn selfColumn = CacheTableInfoUtils.nNRelSelfQueryColumn(relation);
        return QueryWrapper.create()
                .select(CommonStr.ONE)
                .from(queryTable)
                .where(targetColumn.eq(selfColumn));
    }
}
