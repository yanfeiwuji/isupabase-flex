package io.github.yanfeiwuji.isupabase.request.req;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Table;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.RelationManager;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.util.MapUtil;
import com.mybatisflex.core.util.MapperUtil;
import io.github.yanfeiwuji.isupabase.flex.DepthRelQueryExt;
import io.github.yanfeiwuji.isupabase.flex.RelationManagerExt;
import io.github.yanfeiwuji.isupabase.request.filter.Filter;
import io.github.yanfeiwuji.isupabase.request.select.RelQueryInfo;
import io.github.yanfeiwuji.isupabase.request.select.Select;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ParamKeyUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;

/**
 * select
 * simple-> select * from tbl where filters
 * with sub -> select * from tbl
 * left join rel1 on link
 * left join rel2 on link
 * where xxx
 * order by ***
 * 不 order by 使用 exists
 * <p>
 * select * from sys_user where exists (
 * select 1 from sys_role_user
 * left join sys_role on sys_role.id = sys_role_user.rid
 * where sys_role.role_name like '%%' and sys_user.id = sys_role_user.uid
 * ) and sys_user.user_name like '%%' order by sys_user.user_name desc
 * <p>
 * order by only can use in to-one end
 * with order
 */
@Data
@Slf4j
public class ApiReq {

    //
    private Select select;
    // root
    private String tableName;
    private List<Filter> filters;
    private List<String> subTables;
    private Map<String, QueryCondition> subFilters = Map.of();
    private RelQueryInfo relQueryInfo;


    public ApiReq(ServerRequest request, String tableName) {

        log.info("start time:{}", System.currentTimeMillis());
        MultiValueMap<String, String> params = request.params();
        HttpMethod method = request.method();

        this.tableName = tableName;
        TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
        this.select = handlerSelect(params, tableInfo);

        if (method.equals(HttpMethod.GET)) {
            this.subTables = this.select.allRelPres();
            this.relQueryInfo = this.select.tpRelQueryInfo();
            this.handlerSubFilter();
        }


        this.filters = handlerHorizontalFilter(params, tableInfo);
    }

    private Select handlerSelect(MultiValueMap<String, String> params, TableInfo tableInfo) {
        String selectValue = Optional
                .ofNullable(params.getFirst(ParamKeyUtils.SELECT_KEY))
                .orElse("*");
        return new Select(selectValue, tableInfo);
    }

    private List<Filter> handlerHorizontalFilter(MultiValueMap<String, String> params, TableInfo tableInfo) {
        return params.entrySet()
                .stream()
                .filter(it -> subTables.stream().noneMatch(rel -> it.getKey().startsWith(rel)))
                .filter(it -> ParamKeyUtils.canFilter(it.getKey()))
                .flatMap(kv -> kv.getValue().stream().map(v -> new Filter(kv.getKey(), v, tableInfo)))
                .toList();
    }

    // single
    public QueryWrapper queryWrapper() {
        QueryWrapper queryWrapper = QueryWrapper.create();
        return queryWrapper.select(select.getQueryColumns())
                .where(filtersToQueryCondition());
    }

    public List<?> result(BaseMapper baseMapper) {
        if (subFilters.isEmpty()) {
            if (subTables.isEmpty()) {
                return singleTableResult(baseMapper);
            } else {
                return singleTableWithRelResult(baseMapper);
            }
        } else {

        }

        return List.of();
    }

    private void handlerSubFilter() {
        Table<Integer, String, DepthRelQueryExt> depthRelQueryExtTable = this.relQueryInfo.depthRelQueryExt();
        Table<Integer, String, String> integerStringStringTable = this.relQueryInfo.depthRelPre();

        depthRelQueryExtTable.cellSet().forEach(it -> {
            System.out.println(it.getRowKey() + ":" + it.getColumnKey() + "  "
                    + integerStringStringTable.get(it.getRowKey(), it.getColumnKey())

            );

        });

    }

    private List<?> singleTableResult(BaseMapper baseMapper) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.select(select.getQueryColumns());
        queryWrapper.where(filtersToQueryCondition());
        return baseMapper.selectListByQuery(queryWrapper);
    }

    private List<?> singleTableWithRelResult(BaseMapper baseMapper) {
        long start = System.currentTimeMillis();
        log.info("start time:{}", start);
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.select(select.getQueryColumns());
        queryWrapper.where(filtersToQueryCondition());

        log.info("pre query time:{}", System.currentTimeMillis() - start);
        List list = baseMapper.selectListByQuery(queryWrapper);


        RelationManagerExt.setDepthRelQueryExts(this.relQueryInfo.depthRelQueryExt());
        RelationManagerExt.setMaxDepth(this.relQueryInfo.maxDepth());

        RelationManagerExt.queryRelationsWithDepthRelQuery(baseMapper, list);
        log.info("total time:{}", System.currentTimeMillis() - start);
        return list;
    }

    private QueryCondition filtersToQueryCondition() {
        return filters.stream().map(Filter::toQueryCondition).reduce(QueryCondition::and)
                .orElse(QueryCondition.createEmpty());
    }

    public void handler(QueryChain<?> queryChain) {
        queryChain.select(select.getQueryColumns());
        queryChain.where(
                        filters.stream().map(Filter::toQueryCondition).reduce(QueryCondition::and)
                                .orElse(QueryCondition.createEmpty()))
                .withRelations();
    }

}
