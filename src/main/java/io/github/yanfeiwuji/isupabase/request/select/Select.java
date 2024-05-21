package io.github.yanfeiwuji.isupabase.request.select;

import com.mybatisflex.core.query.QueryColumn;
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
import java.util.stream.Collectors;

@Data
@Slf4j
public class Select {
    // id,name,xxx
    // default
    private String selectValue;

    // a,b,roles(a,users(*))
    // roles is roles.
    // users is roles.users
    private String relPre;

    // table query columns
    private List<QueryColumn> queryColumns;

    // this parent to sub rel
    private AbstractRelation<?> relation;

    private TableInfo tableInfo;
    // sub select
    private List<Select> subSelect;

    public Select(String selectValue, TableInfo tableInfo) {
        this(selectValue, tableInfo, null, null);
    }

    public Select(String selectValue, TableInfo tableInfo, AbstractRelation<?> relation, String preRel) {
        log.info("selectValue:{}", selectValue);
        this.selectValue = selectValue;
        this.relation = relation;
        this.tableInfo = tableInfo;
        this.relPre = preRel;

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

        Map<Boolean, List<String>> groupByIsRelFormat = selects.stream()
                .collect(Collectors.groupingBy(MTokens.SELECT_WITH_SUB::find));

        List<String> others = Optional.ofNullable(groupByIsRelFormat.get(false)).orElse(List.of());

        if (!others.isEmpty()) {
            throw MDbExManagers.UNDEFIDEND_COLUMN.reqEx(tableInfo.getTableName(), others.getFirst());
        }
        // hand sub
        Map<Boolean, List<KeyValue>> groupByIsRel = Optional
                .ofNullable(groupByIsRelFormat.get(true))
                .orElse(List.of())
                .stream()
                .map(MTokens.SELECT_WITH_SUB::keyValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.groupingBy(it -> CacheTableInfoUtils.relInTable(it.key(), tableInfo)));

        List<KeyValue> notRelButFormat = Optional.ofNullable(groupByIsRel.get(false)).orElse(List.of());

        if (!notRelButFormat.isEmpty()) {
            throw MDbExManagers.UNDEFIDEND_COLUMN.reqEx(tableInfo.getTableName(), notRelButFormat.getFirst().key());
        }

        this.subSelect = Optional.ofNullable(groupByIsRel.get(true))
                .orElse(List.of()).stream()
                .map(it -> {
                    AbstractRelation<?> realRelation = CacheTableInfoUtils.nNRealRelation(it.key(), tableInfo);
                    return new Select(it.value(), realRelation.getTargetTableInfo(), realRelation,
                            preRel == null ? "%s".formatted(it.key()) : "%s.%s".formatted(preRel, it.key()));
                }).toList();

    }

    public List<QueryColumn> columnsWithSub() {
        queryColumns.addAll(this.subSelect.stream().flatMap(it -> it.columnsWithSub().stream()).toList());
        return queryColumns;
    }

    public List<AbstractRelation<?>> abstractRelations(List<AbstractRelation<?>> result) {
        result.add(relation);
        this.subSelect.stream().forEach(it -> it.abstractRelations(result).stream());
        return result.stream().filter(Objects::nonNull).toList();
    }

    public List<AbstractRelation<?>> abstractRelations() {
        return abstractRelations(new ArrayList<>());
    }

    public List<String> allRelPres(List<String> result) {
        result.add(this.relPre);
        this.subSelect.stream().forEach(it -> it.allRelPres(result));
        return result.stream().filter(Objects::nonNull).toList();
    }

    public List<String> allRelPres() {
        return allRelPres(new ArrayList<>());
    }

}
