package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

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



}
