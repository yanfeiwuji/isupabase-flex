package io.github.yanfeiwuji.isupabase.request.req;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import cn.hutool.json.JSONUtil;
import io.github.yanfeiwuji.isupabase.request.filter.Filter;
import io.github.yanfeiwuji.isupabase.request.select.Select;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ParamKeyUtils;
import lombok.Data;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;

/**
 * select
 * simple->  select * from tbl where filters
 * with sub -> select * from tbl
 * left join rel1 on link
 * left join rel2 on link
 * where xxx
 * order by ***
 * 不 order by 使用 exists
 * <p>
 * select * from sys_user where  exists (
 * select 1 from sys_role_user
 * left join sys_role on sys_role.id = sys_role_user.rid
 * where sys_role.role_name like '%%' and  sys_user.id = sys_role_user.uid
 * ) and sys_user.user_name like '%%' order by sys_user.user_name desc
 * <p>
 * order by  only can use in to-one end
 * with order
 */
@Data
public class ApiReq {
    private Select select;
    // root
    private TableInfo tableInfo;
    private List<Filter> filters;
    private List<Filter> subFilter;

    public ApiReq(ServerRequest request, TableInfo tableInfo) {
        MultiValueMap<String, String> params = request.params();
        this.tableInfo = tableInfo;
        this.select = handlerSelect(params, tableInfo);

        System.out.println( select.allRelPres());
        this.filters = handlerHorizontalFilter(params, tableInfo);
    }

    public Select handlerSelect(MultiValueMap<String, String> params,
                                TableInfo tableInfo) {
        String selectValue = Optional.ofNullable(params.getFirst(ParamKeyUtils.SELECT_KEY))
                .orElse("*");
        return new Select(selectValue, tableInfo);
    }

    public List<Filter> handlerHorizontalFilter(MultiValueMap<String, String> params,
                                                TableInfo tableInfo) {
        return params.entrySet()
                .stream()
                .filter(it -> ParamKeyUtils.canFilter(it.getKey()))
                .flatMap(kv -> kv.getValue().stream().map(v -> new Filter(kv.getKey(), v, tableInfo)))
                .toList();
        // .map(Filter::toQueryCondition)
        // .reduce(QueryCondition::and).orElse(QueryCondition.createEmpty());
    }

    public void handler(QueryChain<?> queryChain) {
        queryChain.select(select.getQueryColumns());

        // .leftJoin(Object.class).on("null")
        queryChain.where(
                filters.stream().map(Filter::toQueryCondition).reduce(QueryCondition::and)
                        .orElse(QueryCondition.createEmpty()));

    }


}
