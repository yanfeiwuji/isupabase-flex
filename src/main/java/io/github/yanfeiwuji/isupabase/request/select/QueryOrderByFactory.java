package io.github.yanfeiwuji.isupabase.request.select;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class QueryOrderByFactory {
    public QueryOrderBy of(QueryColumn queryColumn, String orderType, String nullsType) {
        QueryOrderBy queryOrderBy = CharSequenceUtil.equals(CommonStr.DESC, orderType) ? queryColumn.desc() : queryColumn.asc();
        if (CharSequenceUtil.equals(CommonStr.NULLS_FIRST, nullsType)) {
            queryOrderBy.nullsFirst();
        } else if (CharSequenceUtil.equals(CommonStr.NULLS_LAST, nullsType)) {
            queryOrderBy.nullsLast();
        }
        return queryOrderBy;

    }
}
