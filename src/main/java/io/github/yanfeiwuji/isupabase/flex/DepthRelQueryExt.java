package io.github.yanfeiwuji.isupabase.flex;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;

import java.util.List;
import java.util.stream.Collectors;


public record DepthRelQueryExt(List<QueryColumn> selects, QueryCondition condition) {

    public String[] addTargetName(String targetName) {
        boolean hasAll = selects.stream().anyMatch(it -> it.getName().equals(CommonStr.STAR));
        if (hasAll) {
            return new String[]{CommonStr.STAR};
        } else {
            List<String> list = selects.stream().map(QueryColumn::getName)
                    .collect(Collectors.toList());
            list.add(targetName);
            return list.toArray(new String[list.size()]);
        }
    }
}
