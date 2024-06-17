package io.github.yanfeiwuji.isupabase.flex;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 11:48
 */
@Data
@AllArgsConstructor
public class TableOneOperateConfig<C extends AuthContext, T> {

    // all not inset
    private  Function<C, QueryCondition> using;
    //  using update insert
    private  BiConsumer<C, List<T>> checking;
    // delete not
    private  Function<C, List<QueryColumn>> columns;
    // insert update delete
    private  BiConsumer<C, OperateInfo<T>> before;
    //
}
