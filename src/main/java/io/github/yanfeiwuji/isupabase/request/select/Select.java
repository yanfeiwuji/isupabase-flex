package io.github.yanfeiwuji.isupabase.request.select;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.flex.DepthRelQueryExt;
import io.github.yanfeiwuji.isupabase.request.ex.MDbExManagers;
import io.github.yanfeiwuji.isupabase.request.filter.KeyValue;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.MapKeyUtils;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
public class Select {
    // id,name,xxx
    // default
    private String selectValue;

    // table query columns
    private List<QueryColumn> queryColumns;

    // this parent to sub rel
    // private AbstractRelation<?> relation;
    // a,b,roles(a,users(*))
    // roles is roles.
    // users is roles.users
    private String relPre;
    private String relName;
    private RelParamKeyTableName relParamKeyTableName;

    private String tableName;
    /**
     * is inner
     */
    private boolean inner;
    // sub select
    private List<Select> subSelect;

    public Select(String selectValue, TableInfo tableInfo) {
        this(selectValue, tableInfo, null, null, null, false);
    }

    public Select(String selectValue,
            TableInfo tableInfo,
            String preRel,
            String relName,
            RelParamKeyTableName relParamKeyTableName,
            boolean inner) {

        log.info("selectValue:{}", selectValue);
        this.selectValue = selectValue;

        this.tableName = tableInfo.getTableName();
        this.relPre = preRel;
        this.relName = relName;
        this.relParamKeyTableName = relParamKeyTableName;
        this.inner = inner;
        this.queryColumns = new ArrayList<>();

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
                .collect(Collectors.groupingBy(
                        it -> CacheTableInfoUtils.relInTable(
                                CharSequenceUtil.replaceLast(it.key(),
                                        CommonStr.SELECT_INNER_MARK, ""),
                                tableInfo)));

        List<KeyValue> notRelButFormat = Optional.ofNullable(groupByIsRel.get(false)).orElse(List.of());

        if (!notRelButFormat.isEmpty()) {
            throw MDbExManagers.UNDEFIDEND_COLUMN.reqEx(tableInfo.getTableName(),
                    notRelButFormat.getFirst().key());
        }

        this.subSelect = Optional.ofNullable(groupByIsRel.get(true))
                .orElse(List.of()).stream()
                .map(it -> {
                    String needKey = CharSequenceUtil.replaceLast(it.key(),
                            CommonStr.SELECT_INNER_MARK, "");
                    AbstractRelation<?> realRelation = CacheTableInfoUtils.nNRealRelation(needKey,
                            tableInfo);
                    return new Select(
                            it.value(),
                            realRelation.getTargetTableInfo(),
                            preRel == null ? "%s".formatted(needKey)
                                    : "%s.%s".formatted(preRel,needKey),
                            realRelation.getName(),
                            new RelParamKeyTableName(needKey, tableInfo.getTableName()),
                            it.key().endsWith(CommonStr.SELECT_INNER_MARK));
                }).toList();

    }

    private List<String> allRelPres(List<String> result) {
        result.add(this.relPre);
        this.subSelect.forEach(it -> it.allRelPres(result));
        return result;
    }

    public List<String> allRelPres() {
        return allRelPres(new ArrayList<>()).stream().filter(Objects::nonNull).toList();
    }

    public RelQueryInfo tpRelQueryInfo() {
        Table<Integer, String, DepthRelQueryExt> depthRelQueryExtTable = HashBasedTable.create();

        Table<Integer, String, String> depthRelPre = HashBasedTable.create();
        Table<Integer, String, AbstractRelation<?>> depthRelation = HashBasedTable.create();

        Table<Integer, String, Boolean> inners = HashBasedTable.create();

        List<Select> currentSelectList = List.of(this);

        int depth = -1;
        while (!currentSelectList.isEmpty()) {
            for (Select select : currentSelectList) {
                if (Objects.nonNull(select.getRelName())) {
                    depthRelQueryExtTable.put(
                            depth,
                            select.getRelName(),
                            new DepthRelQueryExt(select.queryColumns,
                                    QueryCondition.createEmpty()));
                    depthRelPre.put(
                            depth,
                            select.getRelName(),
                            select.getRelPre());
                    if (Objects.nonNull(select.relParamKeyTableName)) {
                        depthRelation.put(
                                depth,
                                select.getRelName(),
                                select.relParamKeyTableName.toRelation());
                    }
                    inners.put(
                            depth,
                            select.getRelName(),
                            select.isInner());
                }
            }
            depth += 1;
            currentSelectList = currentSelectList.stream().flatMap(it -> it.subSelect.stream()).toList();

        }
        List<RelInner> relInners = Optional.ofNullable(this.subSelect)
                .orElse(List.of())

                .stream()
                .filter(Select::isInner)
                .map(it -> it.relParamKeyTableName)
                .filter(Objects::nonNull)
                .map(RelParamKeyTableName::toRelInner)
                .toList();

        return new RelQueryInfo(depth, depthRelPre, depthRelQueryExtTable, depthRelation, relInners);
    }

}
