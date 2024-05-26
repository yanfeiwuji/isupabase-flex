package io.github.yanfeiwuji.isupabase.request.req;

import static com.mybatisflex.core.query.QueryMethods.null_;

import java.util.*;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;

import com.google.common.collect.Table;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.*;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import io.github.yanfeiwuji.isupabase.flex.DepthRelQueryExt;
import io.github.yanfeiwuji.isupabase.flex.RelationManagerExt;
import io.github.yanfeiwuji.isupabase.request.filter.Filter;
import io.github.yanfeiwuji.isupabase.request.order.Order;
import io.github.yanfeiwuji.isupabase.request.range.Range;
import io.github.yanfeiwuji.isupabase.request.select.QueryExec;
import io.github.yanfeiwuji.isupabase.request.select.QueryExecFactory;
import io.github.yanfeiwuji.isupabase.request.select.QueryExecStuff;
import io.github.yanfeiwuji.isupabase.request.select.RelInner;
import io.github.yanfeiwuji.isupabase.request.select.RelQueryInfo;
import io.github.yanfeiwuji.isupabase.request.select.Select;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ParamKeyUtils;
import io.github.yanfeiwuji.isupabase.request.utils.QueryWrapperUtils;
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

    private String tableName;
    private List<Filter> filters;
    private List<String> subTables;
    private Range range;
    private RelQueryInfo relQueryInfo;

    private List<Order> rootOrder;

    private QueryExec queryExec;

    public ApiReq(ServerRequest request, String tableName) {
        long s = System.currentTimeMillis();
        log.info("start time:{}", s);
        MultiValueMap<String, String> params = request.params();
        HttpMethod method = request.method();

        this.tableName = tableName;
        log.info("finish to rela  subquery time:{}", System.currentTimeMillis() - s);
        TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
        this.select = handlerSelect(params, tableInfo);

        this.range = ParamKeyUtils.rootRange(params);

        if (method.equals(HttpMethod.GET)) {
            this.subTables = this.select.allRelPres();
            this.relQueryInfo = this.select.tpRelQueryInfo();
            this.handlerSubFilterAndRange(params);
            log.info("finish to handler subquery time:{}", System.currentTimeMillis() - s);
        }

        this.filters = handlerHorizontalFilter(params, tableInfo);
        log.info("finish to need time:{}", System.currentTimeMillis() - s);
    }

    private Select handlerSelect(MultiValueMap<String, String> params, TableInfo tableInfo) {
        String selectValue = Optional
                .ofNullable(params.getFirst(ParamKeyUtils.SELECT_KEY))
                .orElse("*");

        QueryExec queryExec = QueryExecFactory.of(new QueryExecStuff(selectValue, tableInfo, false, null));
        this.queryExec = queryExec;
        Map<String, QueryExec> q = QueryExecFactory.toMap(queryExec, new HashMap<>(), "");
        q.forEach((k, v) -> {
            System.out.println(k + ":" + v.getRelation().getName());
        });
        System.out.println(queryExec + "==");
        return new Select(selectValue, tableInfo);
    }

    /**
     *
     * @param params
     * @param tableInfo
     * @return
     */
    private List<Order> handlerRootOrder(MultiValueMap<String, String> params, TableInfo tableInfo) {
        String orderValue = params.getFirst(ParamKeyUtils.ORDER_KEY);
        Table<Integer, String, String> depthRelPreTable = this.relQueryInfo.depthRelPre();
        Table<Integer, String, AbstractRelation<?>> depthRelationTable = this.relQueryInfo.depthRelation();
        this.relQueryInfo.depthRelPre();
        this.select.getSubSelect().stream().map(it -> it.getRelParamKeyTableName())
                .map(it -> it.toRelInner())
                .forEach(relInner -> {

                });

        ;

        return List.of();
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
        queryWrapper.select(select.getQueryColumns())
                .where(filtersToQueryCondition());

        QueryWrapperUtils.handlerQueryWrapperRange(range, queryWrapper);
        return queryWrapper;
    }

    public List<?> result(BaseMapper<?> baseMapper) {

        return baseMapper.selectListByQuery(queryExec.handler(QueryWrapper.create()));
        // if (subTables.isEmpty()) {
        // return singleTableResult(baseMapper);
        // } else {
        // return singleTableWithRelResult(baseMapper);
        // }
    }

    private void handlerSubFilterAndRange(MultiValueMap<String, String> params) {
        Table<Integer, String, DepthRelQueryExt> depthRelQueryExtTable = this.relQueryInfo.depthRelQueryExt();
        Table<Integer, String, String> depthRelPreTable = this.relQueryInfo.depthRelPre();
        Table<Integer, String, AbstractRelation<?>> depthRelationTable = this.relQueryInfo.depthRelation();
        Table<Integer, String, List<RelInner>> depthInnersTable = this.relQueryInfo.depthInners();

        depthRelQueryExtTable.cellSet().forEach(it -> {
            DepthRelQueryExt depthRelQueryExt = it.getValue();

            String pre = depthRelPreTable.get(it.getRowKey(), it.getColumnKey());
            if (Objects.isNull(pre)) {
                return;
            }
            AbstractRelation<?> abstractRelation = depthRelationTable.get(it.getRowKey(), it.getColumnKey());
            if (Objects.isNull(abstractRelation)) {
                return;
            }
            Class<?> targetEntityClass = abstractRelation.getTargetEntityClass();
            TableInfo tableInfo = TableInfoFactory.ofEntityClass(targetEntityClass);
            if (Objects.isNull(tableInfo)) {
                return;
            }
            params.entrySet()
                    .stream()
                    .filter(kv -> ParamKeyUtils.canSubFilter(kv.getKey(), pre))
                    .flatMap(kv -> kv.getValue().stream().map(v -> new Filter(
                            CharSequenceUtil.removePrefix(kv.getKey(), pre + StrPool.DOT),
                            v, tableInfo)))
                    .map(Filter::toQueryCondition).reduce(QueryCondition::and)
                    .ifPresent(depthRelQueryExt::setCondition);

            Optional.ofNullable(depthInnersTable.get(it.getRowKey(), it.getColumnKey()))
                    .ifPresent(depthRelQueryExt::setRelInners);

            Optional.ofNullable(ParamKeyUtils.preRange(params, pre))
                    .ifPresent(depthRelQueryExt::setRange);

        });

    }

    private List<?> singleTableResult(BaseMapper<?> baseMapper) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.select(select.getQueryColumns());
        queryWrapper.where(filtersToQueryCondition());
        QueryWrapperUtils.handlerQueryWrapperRange(range, queryWrapper);
        // this.rootOrder.forEach(queryWrapper::orderBy);
        return baseMapper.selectListByQuery(queryWrapper);
    }

    private List<?> singleTableWithRelResult(BaseMapper<?> baseMapper) {

        QueryWrapper queryWrapper = QueryWrapper.create();

        queryWrapper.select(select.getQueryColumns());
        handlerFirstInner(queryWrapper);
        queryWrapper.and(filtersToQueryCondition());

        QueryWrapperUtils.handlerQueryWrapperRange(range, queryWrapper);
        // this.rootSingleQueryOrders.forEach(queryWrapper::orderBy);

        List<?> list = baseMapper.selectListByQuery(queryWrapper);

        RelationManagerExt.setDepthRelQueryExts(this.relQueryInfo.depthRelQueryExt());
        RelationManagerExt.setMaxDepth(this.relQueryInfo.maxDepth());

        RelationManagerExt.queryRelationsWithDepthRelQuery(baseMapper, list);
        return list;
    }

    private QueryCondition filtersToQueryCondition() {
        return filters.stream()
                .map(Filter::toQueryCondition)
                .reduce(QueryCondition::and)
                .orElse(QueryCondition.createEmpty());
    }

    /**
     * only handler depth zero
     *
     * @param queryWrapper
     */
    public void handlerFirstInner(QueryWrapper queryWrapper) {
        List<RelInner> relInners = relQueryInfo.inners();
        Table<Integer, String, DepthRelQueryExt> depthRelQueryExtTable = relQueryInfo.depthRelQueryExt();

        relInners.forEach(rel -> {
            AbstractRelation<?> relation = rel.getAbstractRelation();
            String relName = relation.getName();
            DepthRelQueryExt depthRelQueryExt = depthRelQueryExtTable.get(0, relName);

            assert depthRelQueryExt != null;
            RelInnerHandler.handlerRelInner(relation, queryWrapper, depthRelQueryExt);
        });
    }

    public void applyOrder(QueryWrapper queryWrapper) {

    }

}
