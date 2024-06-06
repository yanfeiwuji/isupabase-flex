package io.github.yanfeiwuji.isupabase.request.req;

import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.*;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.DbChain;
import com.mybatisflex.core.table.TableInfo;

import com.mybatisflex.core.update.UpdateChain;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.select.*;
import io.github.yanfeiwuji.isupabase.request.utils.CacheJavaType;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.FlexUtils;
import io.github.yanfeiwuji.isupabase.request.utils.PreferUtils;
import lombok.Data;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ApiReq {
    private static final Logger log = LoggerFactory.getLogger(ApiReq.class);
    // 共用 mapper
    private static ObjectMapper mapper;


    private QueryExec queryExec;
    private QueryExecLookup queryExecLookup;

    private Map<String, String> columns;
    private Map<String, String> prefers;
    private String onConflict;
    private Set<String> preferApplied;

    // use to update
    private Set<String> firstBodyKeys;


    // body
    private List<Object> body;
    private HttpMethod httpMethod;
    private BaseMapper<Object> baseMapper;

    public static void init(ObjectMapper mapper) {
        ApiReq.mapper = mapper;
    }


    public ApiReq(ServerRequest request, String tableName, BaseMapper<Object> baseMapper) {
        MultiValueMap<String, String> params = request.params();
        TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
        this.queryExecLookup = QueryExecFactory.of(params, tableInfo);
        this.queryExec = queryExecLookup.queryExec();
        this.baseMapper = baseMapper;
        this.httpMethod = request.method();

        this.columns = Optional.ofNullable(params.getFirst(CommonStr.COLUMNS))
                .map(it -> CharSequenceUtil.split(it, StrPool.COMMA))
                .orElse(List.of())
                .stream().collect(Collectors.toMap(it -> it, it -> it));

        this.prefers = PreferUtils.pickPrefer(request.headers().firstHeader(CommonStr.PREFER_HEADER_KEY));
        this.onConflict = params.getFirst(CommonStr.ON_CONFLICT); // not impl

        if (!request.method().equals(HttpMethod.POST)) {
            // post not handler filter and order and limit
            QueryExecFactory.assembly(queryExecLookup, params);
        }
        readBody(request, tableInfo);


    }

    public List<?> result() {
        return QueryExecInvoke.invoke(queryExec, baseMapper);
    }

    public Long count() {
        final QueryWrapper qw = Optional.ofNullable(queryExec.getQueryWrapper())
                .orElseGet(() -> queryExec.handler(QueryWrapper.create()));
        qw.limit(null, null);
        return baseMapper.selectCountByQuery(qw);

    }

    public void post() {
        if (prefers.containsKey(CommonStr.PREFER_RESOLUTION_MERGE_DUPLICATES)) {
            Db.tx(() -> {
                FlexUtils.insertOrUpdateSelective(baseMapper, body, queryExec.getTableInfo());
                return true;
            });
        } else {
            Db.tx(() -> {
                baseMapper.insertBatch(body);
                return true;
            });
        }
    }

    public void put() {
        body.forEach(baseMapper::update);
    }

    public void patch() {
        final QueryCondition queryCondition = queryExec.getQueryCondition();

        final List<QueryOrderBy> orders = Optional.ofNullable(queryExec.getOrders()).orElse(List.of());
        final Object first = body.getFirst();
        final Map<String, Object> bodyMap = BeanUtil.beanToMap(first);

        final UpdateChain<Object> chain = UpdateChain.of(baseMapper);
        Optional.ofNullable(firstBodyKeys)
                .orElse(Set.of())
                .forEach(it -> chain.set(it, bodyMap.get(CharSequenceUtil.toCamelCase(it))));

        chain.where(queryCondition);
        orders.forEach(chain::orderBy);
        chain.limit(queryExec.getLimit()).update();
        // 查询
        if (prefers.containsKey(CommonStr.PREFER_RETURN_REPRESENTATION)) {
            final QueryWrapper queryWrapper = QueryWrapper.create().select(queryExec.getQueryColumns())
                    .where(queryCondition);
            orders.forEach(queryWrapper::orderBy);
            queryWrapper.limit(queryExec.getOffset());
            this.body = baseMapper.selectListByQuery(queryWrapper);
        }
    }

    public void delete() {
        final TableInfo tableInfo = queryExec.getTableInfo();
        final List<QueryOrderBy> orders = queryExec.getOrders();
        final DbChain dbChain = DbChain.table(tableInfo.getEntityClass())
                .and(queryExec.getQueryCondition());
        orders.forEach(dbChain::orderBy);
        dbChain.limit(queryExec.getLimit());
        if (prefers.containsKey(CommonStr.PREFER_RETURN_REPRESENTATION)) {
            final List<?> objects = dbChain.listAs(tableInfo.getEntityClass());
            this.body = (List<Object>) readIdsThenLoad(baseMapper, queryExec.getTableInfo(), objects);
        }
        dbChain.remove();

    }

    public Object returnInfo() {

        if (prefers.containsKey(CommonStr.PREFER_RETURN_MINIMAL)) {
            addPreferApplied(CommonStr.PREFER_RETURN_MINIMAL);
            return null;
        }
        if (prefers.containsKey(CommonStr.PREFER_RETURN_REPRESENTATION)) {
            addPreferApplied(CommonStr.PREFER_RETURN_REPRESENTATION);
            if (httpMethod.equals(HttpMethod.DELETE)) {
                return body;
            }
            final TableInfo tableInfo = queryExec.getTableInfo();
            return readIdsThenLoad(baseMapper, tableInfo, body);


        }
        addPreferApplied(CommonStr.PREFER_RETURN_MINIMAL);
        return null;
    }

    @SneakyThrows
    private void readBody(ServerRequest request, TableInfo tableInfo) {
        if (request.method().equals(HttpMethod.GET) || request.method().equals(HttpMethod.DELETE)) {
            return;
        }
        final Class<?> entityClass = tableInfo.getEntityClass();
        String strBody = request.body(String.class);
        if (JSONUtil.isTypeJSONArray(strBody)) {
            JavaType listType = CacheJavaType.listJavaType(entityClass, mapper);
            JavaType listMapJavaType = CacheJavaType.listJavaType(Map.class, mapper);

            final List<Map> mapList = mapper.readValue(strBody, listMapJavaType);

            Optional.ofNullable(mapList.getFirst()).map(Map::keySet).ifPresent(this::setFirstBodyKeys);
            this.body = mapper.readValue(strBody, listType);
        } else {
            final Map map = mapper.readValue(strBody, Map.class);

            Optional.ofNullable(map).map(Map::keySet).ifPresent(this::setFirstBodyKeys);
            this.body = List.of(mapper.readValue(strBody, entityClass));
        }


        if (!columns.isEmpty()) {
            final CopyOptions copyOptions = CopyOptions.create().setPropertiesFilter((f, o) -> {

                final String paramKey = CacheTableInfoUtils.propertyToParamKey(f.getName());
                return columns.containsKey(paramKey);
            });
            // copy
            this.body = (List<Object>) BeanUtil.copyToList(this.body, entityClass, copyOptions);

        }

    }

    private List<?> readIdsThenLoad(BaseMapper<?> baseMapper, TableInfo tableInfo, List<?> body) {

        final String[] primaryColumns = tableInfo.getPrimaryColumns();
        // ids
        final List<Object[]> list = body.stream().map(tableInfo::buildPkSqlArgs)
                .toList();
        final QueryColumn queryColumn = CacheTableInfoUtils.nNRealTableIdColumn(tableInfo);
        // todo  test


        if (primaryColumns.length == 1) {
            final List<Object> ids = list.stream().map(it -> it[0]).toList();
            queryExec.setQueryCondition(queryColumn.in(ids));
            return QueryExecInvoke.invoke(queryExec, baseMapper);
        } else {
            queryExec.setQueryCondition(queryColumn.in(list));
            return QueryExecInvoke.invoke(queryExec, baseMapper);
        }
    }

    private void addPreferApplied(String prefer) {
        if (preferApplied == null) {
            preferApplied = new HashSet<>();
        }
        preferApplied.add(prefer);
    }

}
