package io.github.yanfeiwuji.isupabase.request.req;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.constant.SqlConsts;
import com.mybatisflex.core.query.*;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.table.TableInfo;

import com.mybatisflex.core.update.UpdateChain;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.flex.PgrstDb;
import io.github.yanfeiwuji.isupabase.request.select.*;
import io.github.yanfeiwuji.isupabase.request.utils.*;
import io.github.yanfeiwuji.isupabase.request.validate.Valid;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.groups.Default;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

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

    private static ObjectMapper mapper;
    private static SpringValidatorAdapter validator;
    private static PgrstDb pgrstDb;

    // op using
    private BaseMapper<Object> baseMapper;


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

    private Object responseBody;
    // 受影响的行数
    private Integer rowNum = 0;
    private HttpStatus httpStatus = HttpStatus.OK;

    public static void init(ObjectMapper mapper, SpringValidatorAdapter validator, PgrstDb pgrstDb) {
        ApiReq.mapper = mapper;
        ApiReq.validator = validator;
        ApiReq.pgrstDb = pgrstDb;
    }

    public ApiReq(ServerRequest request, String tableName, BaseMapper<Object> baseMapper) {

        MultiValueMap<String, String> params = request.params();
        TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
        this.queryExecLookup = QueryExecFactory.of(params, tableInfo);
        this.queryExec = queryExecLookup.queryExec();
        this.baseMapper = baseMapper;
        this.httpMethod = request.method();

        this.columns = Optional.ofNullable(params.getFirst(PgrstStrPool.COLUMNS))
                .map(it -> CharSequenceUtil.split(it, StrPool.COMMA))
                .orElse(List.of())
                .stream().collect(Collectors.toMap(it -> it, it -> it));

        this.prefers = PreferUtils.pickPrefer(request.headers().firstHeader(PgrstStrPool.PREFER_HEADER_KEY));
        this.onConflict = params.getFirst(PgrstStrPool.ON_CONFLICT); // not impl

        if (!request.method().equals(HttpMethod.POST)) {
            // post not handler filter and order and limit
            QueryExecFactory.assembly(queryExecLookup, params);
        }
        readBody(request, tableInfo);
    }

    public void get() {
        List<?> list = QueryExecInvoke.invoke(queryExec, baseMapper, pgrstDb);
        rowNum = list.size();
        responseBody = list;

    }

    public Long count() {

        if (Objects.isNull(queryExec.getLimit()) && Objects.isNull(queryExec.getOffset())) {
            return Long.valueOf(rowNum);
        }
        final QueryWrapper qw = Optional.ofNullable(queryExec.getQueryWrapper())
                .orElseGet(() -> queryExec.handler(QueryWrapper.create()));

        // clear orders
        CPI.setOrderBys(qw, null);
        qw.limit(null, null);
        return pgrstDb.selectCountByQuery(baseMapper, qw);
    }

    public void post() {
        if (prefers.containsKey(PgrstStrPool.PREFER_RESOLUTION_MERGE_DUPLICATES)) {
            Db.tx(() -> {
                FlexUtils.insertOrUpdateSelective(baseMapper, pgrstDb, body, queryExec.getTableInfo());
                return true;
            });
        } else {
            Db.tx(() -> {
                pgrstDb.insertBatch(baseMapper, body);
                return true;
            });
        }
        this.httpStatus = HttpStatus.CREATED;
        rowNum = body.size();
    }

    public void put() {

        // body.forEach(baseMapper::update);
        // rowNum = body.size();
    }

    public void patch() {
        checkLimit();
        final QueryCondition queryCondition = queryExec.getQueryCondition();

        final List<QueryOrderBy> orders = Optional.ofNullable(queryExec.getOrders()).orElse(List.of());
        final Object first = body.getFirst();
        final TableInfo tableInfo = queryExec.getTableInfo();
        final Map<String, String> propertyColumnMapping = tableInfo.getPropertyColumnMapping();
        final Row row = (Row) BeanUtil.beanToMap(first, new Row(), true, propertyColumnMapping::get); // modify

        final QueryWrapper queryWrapper = QueryWrapper.create()
                .from(queryExec.getQueryTable())
                .select(queryExec.getQueryColumns())
                .where(queryCondition);
        orders.forEach(queryWrapper::orderBy);
        queryWrapper.limit(queryExec.getLimit());
        queryWrapper.offset(queryExec.getOffset());
        // pre query then use project
        if (prefers.containsKey(PgrstStrPool.PREFER_RETURN_REPRESENTATION)) {
            this.body = pgrstDb.selectListByQuery(baseMapper, queryWrapper);
            rowNum = this.body.size();
        }
        pgrstDb.selectCountByQuery(baseMapper, queryWrapper);
        pgrstDb.updateRowByQuery(baseMapper, row, queryWrapper);


//        final UpdateChain<Object> chain = UpdateChain.of(baseMapper);
//        Optional.ofNullable(firstBodyKeys)
//                .orElse(Set.of())
//                .forEach(it -> chain.set(it, bodyMap.get(CharSequenceUtil.toCamelCase(it))));
//
//
//        final TableInfo tableInfo = queryExec.getTableInfo();
//        final QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(tableInfo);
//        final QueryColumn idColumn = CacheTableInfoUtils.nNRealTableIdColumn(tableInfo);
//        final QueryWrapper updateWrapper = QueryWrapper.create().select(idColumn).from(queryTable);
//
//        updateWrapper.where(queryCondition);
//        orders.forEach(updateWrapper::orderBy);
//        updateWrapper.limit(queryExec.getLimit());
//        updateWrapper.offset(queryExec.getOffset());
//        final QueryWrapper tempWrapper = QueryWrapper.create().select(idColumn).from(updateWrapper).as(PgrstStrPool.UPDATE_TEMP_TABLE);
//
//        chain.where(idColumn.in(tempWrapper)).update();

    }

    public void delete() {
        checkLimit();
        final TableInfo tableInfo = queryExec.getTableInfo();
        final List<QueryOrderBy> orders = Optional.ofNullable(queryExec.getOrders()).orElse(List.of());

        final QueryWrapper queryWrapper = QueryWrapper.create().from(tableInfo.getEntityClass());
        queryWrapper.where(queryExec.getQueryCondition());
        orders.forEach(queryWrapper::orderBy);
        queryWrapper.limit(queryExec.getLimit());
        queryWrapper.offset(queryExec.getOffset());


        if (prefers.containsKey(PgrstStrPool.PREFER_RETURN_REPRESENTATION)) {

            // must query then delete
            final List<?> objects = pgrstDb.selectListByQuery(baseMapper, queryWrapper);
            List<?> res = readIdsThenLoad(baseMapper, queryExec.getTableInfo(), objects);
            rowNum = res.size();
            responseBody = res;
        }

//        final QueryColumn queryColumn = CacheTableInfoUtils.nNRealTableIdColumn(tableInfo);
//        final QueryTable queryTable = CacheTableInfoUtils.nNQueryTable(tableInfo);
//        final QueryWrapper deleteWithOrderLimit = QueryWrapper.create().select(queryColumn)
//                .from(queryTable)
//                .where(queryExec.getQueryCondition());
//        orders.forEach(deleteWithOrderLimit::orderBy);
//        deleteWithOrderLimit.limit(queryExec.getLimit());
//        deleteWithOrderLimit.offset(queryExec.getOffset());
        pgrstDb.deleteByQuery(baseMapper, queryWrapper);
    }

    public void returnInfo() {
        if (httpMethod.equals(HttpMethod.GET)) {
            // get not change
            return;
        }
        if (prefers.containsKey(PgrstStrPool.PREFER_RETURN_MINIMAL)) {
            addPreferApplied(PgrstStrPool.PREFER_RETURN_MINIMAL);
            responseBody = null;
        }
        if (prefers.containsKey(PgrstStrPool.PREFER_RETURN_REPRESENTATION)) {
            addPreferApplied(PgrstStrPool.PREFER_RETURN_REPRESENTATION);
            if (httpMethod.equals(HttpMethod.DELETE)) {
                return;
            } else {
                final TableInfo tableInfo = queryExec.getTableInfo();
                responseBody = readIdsThenLoad(baseMapper, tableInfo, body);

            }
        }
        addPreferApplied(PgrstStrPool.PREFER_RETURN_MINIMAL);

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void readBody(ServerRequest request, TableInfo tableInfo) {
        if (request.method().equals(HttpMethod.GET) || request.method().equals(HttpMethod.DELETE)) {
            return;
        }

        try {
            final Class<?> entityClass = tableInfo.getEntityClass();
            String strBody = request.body(String.class);

            if (ValueUtils.isTypeJSONArray(strBody)) {
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
            // validator
            validatorBody();

            if (!columns.isEmpty()) {
                final CopyOptions copyOptions = CopyOptions.create().setPropertiesFilter((f, o) -> {
                    final String paramKey = CacheTableInfoUtils.propertyToParamKey(f.getName());
                    return columns.containsKey(paramKey);
                });
                // copy
                this.body = (List<Object>) BeanUtil.copyToList(this.body, entityClass, copyOptions);

            }
        } catch (ServletException | IOException e) {
            throw PgrstExFactory.exInvalidJson().get();
        }
    }

    public ServerResponse handler() {
        // ok.header().header().header()

        switch (httpMethod.name()) {
            case "GET" -> get();
            case "POST" -> post();
            case "PATCH" -> patch();
            // case "PUT" -> put();
            case "DELETE" -> delete();
            default -> {
                // todo
            }
        }
        returnInfo();

        if (prefers.containsKey(PgrstStrPool.PREFER_COUNT_EXACT) && HttpMethod.GET.equals(httpMethod)) {
            httpStatus = HttpStatus.PARTIAL_CONTENT;
        }

        if (Objects.nonNull(responseBody)) {
            return ServerResponse.status(httpStatus)
                    .headers(this::addHeader)
                    .body(responseBody);
        } else {

            if (!httpMethod.equals(HttpMethod.POST) || !httpStatus.equals(HttpStatus.CREATED)) {
                httpStatus = HttpStatus.NO_CONTENT;
            }
            return ServerResponse
                    .status(httpStatus)
                    .headers(this::addHeader).build();
        }

    }

    public void addHeader(HttpHeaders headers) {
        String count = PgrstStrPool.STAR;
        if (prefers.containsKey(PgrstStrPool.PREFER_COUNT_EXACT)) {
            addPreferApplied(PgrstStrPool.PREFER_COUNT_EXACT);
            if (httpMethod.equals(HttpMethod.GET)) {
                count = count().toString();
            } else {
                count = rowNum.toString();
            }
        }

        final Number offset = Optional.ofNullable(queryExec.getOffset()).orElse(0);
        final Number limit = Optional.ofNullable(queryExec.getLimit()).orElse(rowNum);

        headers.add(PgrstStrPool.HEADER_RANGE_KEY,
                PgrstStrPool.HEADER_RANGE_VALUE_FORMAT.formatted(
                        limit.intValue() - 1 >= 0 ? PgrstStrPool.HEADER_RANGE_VALUE_RANGE_FORMAT
                                .formatted(offset.intValue(), offset.intValue() + limit.intValue() - 1) : PgrstStrPool.STAR,
                        count));

        headers.add(PgrstStrPool.HEADER_PREFERENCE_APPLIED_KEY, CharSequenceUtil.join(StrPool.COMMA, preferApplied));
        // headers.setAccessControlAllowHeaders(CommonStr.ACCESS_CONTROL_EXPOSE_HEADERS);
    }

    private List<?> readIdsThenLoad(BaseMapper<?> baseMapper, TableInfo tableInfo, List<?> body) {

        final QueryCondition queryCondition = inIdsCondition(tableInfo, body);
        if (Objects.isNull(queryCondition)) {
            return List.of();
        }
        queryExec.setQueryCondition(queryCondition);
        // final List<?> invoke = QueryExecInvoke.invoke(queryExec, baseMapper);

        return QueryExecInvoke.invoke(queryExec, baseMapper, pgrstDb);
    }

    private QueryCondition inIdsCondition(TableInfo tableInfo, List<?> body) {

        final String[] primaryColumns = tableInfo.getPrimaryColumns();
        // ids
        final List<Object[]> list = body.stream().map(tableInfo::buildPkSqlArgs)
                .toList();
        final QueryColumn queryColumn = CacheTableInfoUtils.nNRealTableIdColumn(tableInfo);
        // to test
        if (list.isEmpty()) {
            return null;
        }
        if (primaryColumns.length == 1) {
            final List<Object> ids = list.stream().map(it -> it[0]).toList();
            return queryColumn.in(ids);
        } else {
            return queryColumn.in(list);
        }

    }

    private void addPreferApplied(String prefer) {
        if (preferApplied == null) {
            preferApplied = new HashSet<>();
        }
        preferApplied.add(prefer);
    }

    private void checkLimit() {
        final List<QueryOrderBy> orders = queryExec.getOrders();
        final Number limit = queryExec.getLimit();

        if (Objects.isNull(limit)) {
            return;
        }

        if (Objects.isNull(orders) || orders.isEmpty()) {
            throw PgrstExFactory.exUpdateOrDeleteUseLimitMustHasOrderUniCol().get();
        }
    }

    private void validatorBody() {
        if (Objects.isNull(body)) {
            return;
        }
        Set<ConstraintViolation<Object>> errors = Set.of();
        if (HttpMethod.PATCH.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod)) {
            errors = validator.validate(body, Default.class, Valid.Update.class);
        }
        if (HttpMethod.POST.equals(httpMethod)) {
            errors = validator.validate(body.getFirst(), Default.class, Valid.Insert.class);
        }
        if (!errors.isEmpty()) {
            throw PgrstExFactory.exInsertValidatorError(errors, queryExec.getQueryTable().getName()).get();
        }
    }

}
