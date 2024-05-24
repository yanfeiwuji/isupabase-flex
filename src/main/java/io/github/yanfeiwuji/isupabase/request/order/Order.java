package io.github.yanfeiwuji.isupabase.request.order;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Data
public class Order {
    private List<QueryOrderBy> orders = new ArrayList<>();

    public Order(String paramValue, TableInfo tableInfo) {

        Stream.of(paramValue.split(StrPool.COMMA))
                .map(MTokens.ORDER_BY::groups)
                .map(it -> {
                    System.out.println(it);
                    return null;
                });


    }


}
