package io.github.yanfeiwuji.isupabase.request.select;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.SelectUtils;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QueryExecFactory {

    public QueryExec of(QueryExecStuff stuff) {

        QueryExec queryExec = new QueryExec();
        TableInfo tableInfo = stuff.tableInfo();

        queryExec.setQueryTable(CacheTableInfoUtils.nNQueryTable(tableInfo));
        queryExec.setRelation(stuff.relation());
        queryExec.setInner(stuff.inner());

        List<String> seletItmes = TokenUtils.splitByComma(stuff.select());
        seletItmes.forEach(selectItem -> {
            if (MTokens.SELECT_WITH_SUB.find(selectItem)) {

                QueryExec subQueryExec = QueryExecFactory.of(SelectUtils.queryExecStuff(selectItem, tableInfo));
                queryExec.addSub(subQueryExec);
            } else {
                queryExec.addQuerycolum(SelectUtils.queryColumn(selectItem, tableInfo));
            }
        });

        return queryExec;
    }

    // useInfo rel info
    public Table<Integer, String, QueryExec> toTable(QueryExec queryExec) {
        Table<Integer, String, QueryExec> table = HashBasedTable.create();
        List<QueryExec> currSubs = Optional.ofNullable(queryExec.subs).orElse(List.of());
        Integer depth = 0;
        while (!currSubs.isEmpty()) {
            currSubs.stream()
                    .filter(it -> Objects.nonNull(it.relation))
                    .forEach(exec -> table.put(depth, exec.getRelation().getName(), exec));
            currSubs = currSubs.stream()
                    .filter(it -> Objects.nonNull(it.getSubs()))
                    .flatMap(it -> it.getSubs().stream()).toList();
        }
        return table;
    }

    public Map<String, QueryExec> toMap(QueryExec queryExec, Map<String, QueryExec> result, String pre) {

        String needPre = Optional.ofNullable(queryExec.relation)
                .map(rel -> {
                    String next = CharSequenceUtil.split(rel.getName(), StrPool.DOT).getLast();
                    return CharSequenceUtil.isEmpty(pre) ? next : pre + "." + next;
                })
                .orElse(CommonStr.EMPTY_STRING);

        if (Objects.nonNull(queryExec.relation)) {
            String needKey = CacheTableInfoUtils.propertyToParamKey(needPre);
            result.put(needKey, queryExec);
        }

        Optional.ofNullable(queryExec.subs)
                .orElse(List.of())
                .stream().forEach(it -> toMap(it, result, needPre));

        return result;
    }
}
