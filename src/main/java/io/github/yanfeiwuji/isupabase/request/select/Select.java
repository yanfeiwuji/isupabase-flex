package io.github.yanfeiwuji.isupabase.request.select;

import com.mybatisflex.core.query.Join;
import com.mybatisflex.core.query.Joiner;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.ex.MDbExManagers;
import io.github.yanfeiwuji.isupabase.request.filter.KeyValue;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Slf4j
public class Select {
    // id,name,xxx
    // default
    private String selectValue;
    // table
    private List<QueryColumn> queryColumns = List.of();

    private AbstractRelation<?> relation;
    private List<Select> subSelect;

    public Select(String selectValue, TableInfo tableInfo, AbstractRelation<?> relation) {

        log.info("selectValue:{}", selectValue);
        this.selectValue = selectValue;
        this.relation = relation;
        // TODO sub query use exist then
        queryColumns = new ArrayList<>();

        List<String> selects = TokenUtils.splitByComma(selectValue);

        Map<Boolean, List<String>> groupByInColumn = selects.stream()
                .collect(Collectors.groupingBy(it -> CacheTableInfoUtils.columnInTable(it, tableInfo)));

        if (selects.contains(CommonStr.STAR)) {
            queryColumns.add(CacheTableInfoUtils.nNQueryAllColumns(tableInfo));
        } else {
            Optional.ofNullable(groupByInColumn.get(true))
                    .orElse(List.of())
                    .stream()
                    .map(it -> CacheTableInfoUtils.nNRealQueryColumn(it, tableInfo))
                    .forEach(queryColumns::add);
        }
        selects = Optional.ofNullable(groupByInColumn.get(false))
                .orElse(List.of()).stream().filter(it -> !CommonStr.STAR.equals(it)).toList();

        // selects = selects.stream().filter(it ->
        // !CacheTableInfoUtils.columnInTable(it, tableInfo)).toList();
        Map<Boolean, List<String>> groupByIsRelFormat = selects.stream()
                .collect(Collectors.groupingBy(it -> MTokens.SELECT_WITH_SUB.find(it)));

        List<String> others = Optional.ofNullable(groupByIsRelFormat.get(false)).orElse(List.of());

        if (others.size() != 0) {
            throw MDbExManagers.UNDEFIDEND_COLUMN.reqEx(tableInfo.getTableName(), others.getFirst());
        }
        // hand sub
        Map<Boolean, List<KeyValue>> groupByIsRel = Optional.ofNullable(groupByIsRelFormat.get(true)).orElse(List.of())
                .stream()
                .map(MTokens.SELECT_WITH_SUB::keyValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.groupingBy(it -> CacheTableInfoUtils.relInTable(it.key(), tableInfo)));

        List<KeyValue> notRelButFormat = Optional.ofNullable(groupByIsRel.get(false)).orElse(List.of());

        if (notRelButFormat.size() != 0) {
            throw MDbExManagers.UNDEFIDEND_COLUMN.reqEx(tableInfo.getTableName(), notRelButFormat.getFirst().key());
        }

        this.subSelect = Optional.ofNullable(groupByIsRel.get(true))
                .orElse(List.of()).stream()
                .map(it -> {
                    AbstractRelation<?> realRelation = CacheTableInfoUtils.nNRealRelation(it.key(), tableInfo);
                    return new Select(it.value(), realRelation.getTargetTableInfo(), realRelation);
                }).toList();

    }

    public List<QueryColumn> columnsWithSub() {
        queryColumns.addAll(this.subSelect.stream().flatMap(it -> it.columnsWithSub().stream()).toList());
        return queryColumns;
    }

    public List<AbstractRelation<?>> abstractRelations() {
        List<AbstractRelation<?>> list = new ArrayList<>();
        list.add(relation);
        System.out.println(list);
        System.out.println(this.subSelect + "=");
        list.addAll(this.subSelect.stream().flatMap(it -> it.abstractRelations().stream()).toList());
        return list.stream().filter(Objects::nonNull).toList();
    }
}
