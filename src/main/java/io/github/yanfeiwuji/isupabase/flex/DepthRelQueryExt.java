package io.github.yanfeiwuji.isupabase.flex;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.select.RelInner;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class DepthRelQueryExt {
    private List<QueryColumn> selects;
    private QueryCondition condition;
    private List<RelInner> relInners;

    private Map<String, String> selectsKeysMap;
    private boolean hasAll;

    public DepthRelQueryExt(List<QueryColumn> selects,
                            QueryCondition condition,
                            List<RelInner> relInners
    ) {
        this.selects = selects;
        this.condition = condition;
        this.relInners = relInners;
        this.selectsKeysMap = selects.stream().collect(Collectors.toMap(
                QueryColumn::getName,
                QueryColumn::getName
        ));
        this.hasAll = selects.stream().anyMatch(it -> it.getName().equals(CommonStr.STAR));

    }

    public List<QueryColumn> addTargetColumn(AbstractRelation<?> relation) {
        TableInfo tableInfo = TableInfoFactory.ofEntityClass(relation.getTargetEntityClass());
        if (hasAll) {
            return List.of(CacheTableInfoUtils.nNQueryAllColumns(tableInfo));
        } else {
            QueryColumn queryColumn = CacheTableInfoUtils.nNRelTargetQueryColumn(relation);
            if (selectsKeysMap.containsKey(queryColumn.getName())) {
                return selects;
            } else {
                List<QueryColumn> next = new ArrayList<>(selects);
                next.add(queryColumn);
                return next;
            }
        }
    }

    public boolean needToClearTargetColumn(AbstractRelation<?> relation) {
        if (isHasAll()) {
            return false;
        }
        String column = CacheTableInfoUtils.nNRelTargetQueryColumn(relation).getName();
        return selects.stream().map(QueryColumn::getName).noneMatch(it -> it.equals(column));
    }
}
