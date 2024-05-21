package io.github.yanfeiwuji.isupabase.request.select;

import java.util.Map;

import com.fasterxml.jackson.core.filter.JsonPointerBasedFilter;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import lombok.Data;

@Data
public class RelJoinConditions {

    private AbstractRelation<?> abstractRelation;

    private Map<Class<?>, QueryCondition> joinCondition;

    public RelJoinConditions(AbstractRelation<?> abstractRelation) {
        this.abstractRelation = abstractRelation;

        // todo handler table join
        TableInfo tableInfo = TableInfoFactory.ofTableName(abstractRelation.getJoinTable());

    }
}
