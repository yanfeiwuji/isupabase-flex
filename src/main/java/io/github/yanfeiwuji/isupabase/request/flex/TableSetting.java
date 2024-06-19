package io.github.yanfeiwuji.isupabase.request.flex;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author yanfeiwuji
 * @date 2024/6/19 09:38
 */
@Data
@AllArgsConstructor
public class TableSetting<T> {
    // all not inset
    private Function<PgrstContext, QueryCondition> using;
    //  using update insert
    private BiConsumer<PgrstContext, List<T>> checking;
    // delete not
    private Function<PgrstContext, List<QueryColumn>> columns;


    // insert update delete
    private BiConsumer<PgrstContext, OperateInfo<T>> before;
    // insert update delete
    private BiConsumer<PgrstContext, OperateInfo<T>> after;


}
