package io.github.yanfeiwuji.isupabase.request.select;

import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryOrderBy;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QueryOrderByFactory {
    public QueryOrderBy of(QueryColumn queryColumn, String orderType, String nullsType) {
        QueryOrderBy queryOrderBy =
                CharSequenceUtil.equals(PgrstStrPool.DESC, orderType) ? queryColumn.desc()
                        : queryColumn.asc();
        if (CharSequenceUtil.equals(PgrstStrPool.NULLS_FIRST, nullsType)) {
            queryOrderBy.nullsFirst();
        } else if (CharSequenceUtil.equals(PgrstStrPool.NULLS_LAST, nullsType)) {
            queryOrderBy.nullsLast();
        }
        return queryOrderBy;

    }
}
