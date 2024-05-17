package io.github.yanfeiwuji.isupabase.request.select;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.annotation.RelationOneToMany;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.relation.RelationManager;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Data
@Slf4j
public class Select {

    private String selectValue;
    private List<QueryColumn> queryColumns;
    private List<Select> selects;

    public Select(String selectValue, TableInfo tableInfo) {

        log.info("selectValue:{}", selectValue);
        this.selectValue = selectValue;

        queryColumns = new ArrayList<>();

        List<String> selects = TokenUtils.splitByComma(selectValue);
        selects.forEach(System.out::println);

        if (selects.contains(CommonStr.STAR)) {
            queryColumns.add(CacheTableInfoUtils.nNQueryAllColumns(tableInfo));
        } else {
            selects.stream().filter(it -> CacheTableInfoUtils.columnInTable(it, tableInfo))
                    .map(it -> CacheTableInfoUtils.nNRealQueryColumn(it, tableInfo))
                    .forEach(queryColumns::add);
        }

        selects = selects.stream().filter(it -> !CacheTableInfoUtils.columnInTable(it, tableInfo)).toList();
        // TODO 获取关系
        System.out.println(selects);

        List<AbstractRelation> relations = RelationManager.getRelations(tableInfo.getEntityClass());

        System.out.println(relations);

        TokenUtils.splitByComma(selectValue);

    }

    public void handlerQueryWrapper(QueryWrapper queryWrapper) {
        queryWrapper.select(this.queryColumns);
    }
}
