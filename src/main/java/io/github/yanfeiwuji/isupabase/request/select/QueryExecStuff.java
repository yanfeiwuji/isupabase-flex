package io.github.yanfeiwuji.isupabase.request.select;

import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;

/**
 * QueryExecStuff
 */
public record QueryExecStuff(String select, TableInfo tableInfo, boolean inner, AbstractRelation<?> relation) {
    public QueryExecStuff(String select, TableInfo tableInfo) {
        this(select, tableInfo, false, null);
    }
}