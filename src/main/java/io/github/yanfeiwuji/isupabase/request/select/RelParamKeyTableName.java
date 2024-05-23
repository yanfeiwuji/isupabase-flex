package io.github.yanfeiwuji.isupabase.request.select;

import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

import java.util.ArrayList;

public record RelParamKeyTableName(String paramKey, String tableName) {

    public AbstractRelation<?> toRelation() {
        TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
        return CacheTableInfoUtils.nNRealRelation(paramKey, tableInfo);
    }

    public RelTree toRelTree() {
        TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
        AbstractRelation<?> abstractRelation = CacheTableInfoUtils.nNRealRelation(paramKey, tableInfo);
        return new RelTree(tableInfo, abstractRelation, new ArrayList<>());
    }
}
