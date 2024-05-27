package io.github.yanfeiwuji.isupabase.request.utils;

import java.util.Optional;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.filter.KeyValue;
import io.github.yanfeiwuji.isupabase.request.select.QueryExecStuff;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SelectUtils {

    public QueryColumn queryColumn(String selectItem, TableInfo tableInfo) {
        if (CommonStr.STAR.equals(selectItem)) {
            return CacheTableInfoUtils.nNQueryAllColumns(tableInfo);
        } else {
            return CacheTableInfoUtils.nNRealQueryColumn(selectItem, tableInfo);
        }
    }

    public QueryExecStuff queryExecStuff(String selectItem, TableInfo tableInfo) {
        return MTokens.SELECT_WITH_SUB.keyValue(selectItem)
                .map(keyValue -> {
                    boolean inner = keyValue.key().endsWith(CommonStr.SELECT_INNER_MARK);
                    String key = CharSequenceUtil.replace(keyValue.key(), CommonStr.SELECT_INNER_MARK,
                            CommonStr.EMPTY_STRING);
                    AbstractRelation<?> relation = CacheTableInfoUtils.nNRealRelation(key, tableInfo);
                    TableInfo innerTableInfo = TableInfoFactory.ofEntityClass(relation.getTargetEntityClass());
                    return new QueryExecStuff(keyValue.value(), innerTableInfo, inner, relation);
                })
                // not
                .orElseThrow();

    }

}