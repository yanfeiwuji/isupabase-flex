package io.github.yanfeiwuji.isupabase.request.select;

import java.util.*;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ParamKeyUtils;
import io.github.yanfeiwuji.isupabase.request.utils.SelectUtils;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

@UtilityClass
@Slf4j
public class QueryExecFactory {

    public QueryExecLookup of(MultiValueMap<String, String> params, TableInfo tableInfo) {
        long start = System.currentTimeMillis();

        log.info("start: time:{}", start);
        String selectValue = Optional
                .ofNullable(params.getFirst(ParamKeyUtils.SELECT_KEY))
                .orElse(CommonStr.STAR);

        Map<String, QueryExec> lookup = new HashMap<>();
        QueryExec queryExec = QueryExecFactory
                .of(new QueryExecStuff(selectValue, tableInfo), lookup, CommonStr.EMPTY_STRING);

        log.info("duration: time:{}", System.currentTimeMillis() - start);
        return new QueryExecLookup(queryExec, lookup);
    }

    // one time to get indexed and
    private QueryExec of(QueryExecStuff stuff, Map<String, QueryExec> indexed, String pre) {

        QueryExec queryExec = new QueryExec();
        TableInfo tableInfo = stuff.tableInfo();

        queryExec.setTableInfo(tableInfo);
        queryExec.setQueryTable(CacheTableInfoUtils.nNQueryTable(tableInfo));
        queryExec.setRelation(stuff.relation());
        queryExec.setInner(stuff.inner());

        String needPre = Optional.ofNullable(queryExec.getRelation())
                .map(rel -> CharSequenceUtil.split(rel.getName(), StrPool.DOT).getLast())
                .map(next -> CharSequenceUtil.isEmpty(pre) ? next : pre + StrPool.DOT + next)
                .map(CacheTableInfoUtils::propertyToParamKey)
                .orElse(CommonStr.EMPTY_STRING);
        if (Objects.nonNull(queryExec.getRelation())) {
            indexed.put(needPre, queryExec);
        }


        TokenUtils.splitByComma(stuff.select()).forEach(selectItem -> {
            if (MTokens.SELECT_WITH_SUB.find(selectItem)) {
                QueryExec subQueryExec = QueryExecFactory.of(SelectUtils.queryExecStuff(selectItem, tableInfo), indexed, needPre);
                queryExec.addSub(subQueryExec);
            } else {
                queryExec.addQueryColumn(SelectUtils.queryColumn(selectItem, tableInfo));
            }
        });

        return queryExec;
    }


    public void rig(QueryExecLookup queryExecLookup, MultiValueMap<String, List<String>> params) {
        Map<String, QueryExec> indexed = queryExecLookup.indexed();
        params.forEach((k, values) -> {

            /**
             * k xx.xx
             *
             * value
             * limit
             * offset
             * order
             *
             */
        });
    }
}
