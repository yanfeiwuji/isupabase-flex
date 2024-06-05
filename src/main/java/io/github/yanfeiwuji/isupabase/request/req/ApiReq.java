package io.github.yanfeiwuji.isupabase.request.req;

import java.util.*;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.*;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.table.TableInfo;

import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.BodyInfo;
import io.github.yanfeiwuji.isupabase.request.select.*;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.FlexUtils;
import io.github.yanfeiwuji.isupabase.request.utils.PreferUtils;
import lombok.Data;
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

    private QueryExec queryExec;
    private QueryExecLookup queryExecLookup;

    private List<String> columns;
    private Map<String, String> prefers;
    private String onConflict;


    public ApiReq(ServerRequest request, String tableName) {
        MultiValueMap<String, String> params = request.params();
        TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
        this.queryExecLookup = QueryExecFactory.of(params, tableInfo);
        this.queryExec = queryExecLookup.queryExec();

        columns = Optional.ofNullable(params.getFirst(CommonStr.COLUMNS))
                .map(it -> CharSequenceUtil.split(it, StrPool.COMMA))
                .orElse(List.of());

        prefers = PreferUtils.pickPrefer(request.headers().firstHeader(CommonStr.PREFER_HEADER_KEY));
        onConflict = params.getFirst(CommonStr.ON_CONFLICT);
        if (!request.method().equals(HttpMethod.POST)) {
            // post not handler filter and order and limit
            QueryExecFactory.assembly(queryExecLookup, params);
        }
    }

    public List<?> result(BaseMapper<?> baseMapper) {
        return QueryExecInvoke.invoke(queryExec, baseMapper);
    }

    public Long count(BaseMapper<?> baseMapper) {

        final QueryWrapper qw = Optional.ofNullable(queryExec.getQueryWrapper())
                .orElseGet(() -> queryExec.handler(QueryWrapper.create()));
        qw.limit(null, null);
        return baseMapper.selectCountByQuery(qw);

    }

    public void insert(BaseMapper<Object> baseMapper, BodyInfo<?> bodyInfo) {

        Optional.ofNullable(bodyInfo.getSingle())
                .ifPresent(baseMapper::insert);

        Optional.ofNullable(bodyInfo)
                .map(BodyInfo::getArray)
                .map(it -> (List<Object>) it)
                .ifPresent(it -> {
                    if (prefers.containsKey(CommonStr.PREFER_RESOLUTION_MERGE_DUPLICATES)) {
                        Db.tx(() -> {
                            FlexUtils.insertOrUpdateSelective(baseMapper, it, queryExec.getTableInfo());
                            return true;
                        });

                    } else {
                        Db.tx(() -> {
                            baseMapper.insertBatch(it);
                            return true;
                        });
                    }
                });
    }

    public Object returnInfo(BaseMapper<?> baseMapper, BodyInfo<Object> bodyInfo) {

        if (prefers.containsKey(CommonStr.PREFER_RETURN_MINIMAL)) {
            return null;
        }
        if (prefers.containsKey(CommonStr.PREFER_RETURN_REPRESENTATION)) {
            final TableInfo tableInfo = queryExec.getTableInfo();
            final String[] primaryColumns = tableInfo.getPrimaryColumns();
            // ids
            final List<Object[]> list = bodyInfo.result().stream().map(tableInfo::buildPkSqlArgs)
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
        return null;
    }

    public QueryWrapper queryWrapper() {
        return queryExec.handler(QueryWrapper.create());
    }


}
