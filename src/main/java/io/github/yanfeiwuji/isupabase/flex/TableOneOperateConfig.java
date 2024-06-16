package io.github.yanfeiwuji.isupabase.flex;

import com.mybatisflex.core.query.QueryCondition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 11:48
 */
@Data
@AllArgsConstructor
public class TableOneOperateConfig<T> {

    // all not inset
    private Function<InvokeContext, QueryCondition> using;
    //  using update insert
    private BiConsumer<InvokeContext, List<T>> checking;
    // delete not
    private Function<InvokeContext, List<String>> columns;
    // insert update delete
    private BiConsumer<InvokeContext, OperateInfo<T>> before;
    //


}
