package io.github.yanfeiwuji.isupabase.request.select;

import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

public record RelParamKeyTableName(String paramKey, String tableName) {

    public AbstractRelation<?> toRelation() {
        TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
        return CacheTableInfoUtils.nNRealRelation(paramKey, tableInfo);
    }

    public RelInner toRelInner() {
        TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
        AbstractRelation<?> relation = CacheTableInfoUtils.nNRealRelation(paramKey, tableInfo);
        return new RelInner(tableInfo, relation);
    }

}
