package io.github.yanfeiwuji.isupabase.request.order;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;

import com.mybatisflex.annotation.RelationManyToOne;
import com.mybatisflex.annotation.RelationOneToOne;
import com.mybatisflex.core.activerecord.query.OrderByBuilder;
import com.mybatisflex.core.constant.SqlConsts;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;

import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.ex.MReqExManagers;
import io.github.yanfeiwuji.isupabase.request.select.RelInner;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
public class Order {

    private List<QueryOrderBy> orders;
    // has need to join null not todo
    private List<AbstractRelation<?>> joins;

    public Order(String paramValue, TableInfo tableInfo) {
        // this(paramValue, tableInfo, null);
    }

    // public Order(String paramValue, RelInner relInner) {
    // this.join = relation;
    // if (relation != null) {
    // List<String> value = MTokens.TOP_ORDER_BY.groups(paramValue);

    // if (!(relation instanceof RelationOneToOne || relation instanceof
    // RelationManyToOne)) {
    // throw MReqExManagers.ORDER_NO_APPLY.reqEx(value.get(1));
    // }
    // // List<String> value = MTokens.TOP_ORDER_BY.groups(paramValue);
    // System.out.println(value + "==");
    // }
    // this.order = Optional.ofNullable(paramValue)
    // .map(MTokens.ORDER_BY::groups)
    // .filter(it -> it.size() == 4)
    // .map(it -> {
    // String paramKey = it.get(1);
    // QueryColumn queryColumn = CacheTableInfoUtils.nNRealQueryColumn(paramKey,
    // tableInfo);
    // QueryOrderBy queryOrderBy = Optional.ofNullable(it.get(2))
    // .map(orderType -> {
    // if (orderType.equals(CommonStr.DESC)) {
    // return queryColumn.desc();
    // } else {
    // return queryColumn.asc();
    // }
    // }).orElse(queryColumn.asc());

    // Optional.ofNullable(it.get(3))
    // .ifPresent(nullType -> {
    // if (nullType.equals(CommonStr.NULL_FIRST)) {
    // queryOrderBy.nullsFirst();
    // } else if (nullType.equals(CommonStr.NULL_LAST)) {
    // queryOrderBy.nullsLast();
    // }
    // });

    // return queryOrderBy;
    // }).orElse(List.of());
    // }

    // public void handlerQueryWrapper() {

    // }
}
