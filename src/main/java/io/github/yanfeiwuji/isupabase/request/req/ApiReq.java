package io.github.yanfeiwuji.isupabase.request.req;

import java.util.*;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.*;
import com.mybatisflex.core.table.TableInfo;

import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.select.*;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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


    QueryExec queryExec;
    QueryExecLookup queryExecLookup;
    List<String> columns;

    public ApiReq(ServerRequest request, String tableName) {
        MultiValueMap<String, String> params = request.params();
        TableInfo tableInfo = CacheTableInfoUtils.nNRealTableInfo(tableName);
        QueryExecLookup queryExecLookup = QueryExecFactory.of(params, tableInfo);
        this.queryExec = queryExecLookup.queryExec();
        this.queryExecLookup = queryExecLookup;
        columns = Optional.ofNullable(params.getFirst(CommonStr.COLUMNS))
                .map(it -> StrUtil.split(it, StrUtil.COMMA))
                .orElse(List.of());

        QueryExecFactory.assembly(queryExecLookup, params);
    }


    public List<?> result(BaseMapper<?> baseMapper) {
        List<?> res = QueryExecInvoke.invoke(queryExec, baseMapper);
        System.out.println(count(baseMapper) + "--");
        return res;
    }

    public Long count(BaseMapper<?> baseMapper) {


        final QueryWrapper qw = Optional.ofNullable(queryExec.getQueryWrapper())
                .orElseGet(() -> queryExec.handler(QueryWrapper.create()));
        qw.limit(null, null);
        return baseMapper.selectCountByQuery(qw);

    }

    public void post(BaseMapper<?> baseMapper, ObjectMapper objectMapper) {

    }

    public QueryWrapper queryWrapper() {
        return queryExec.handler(QueryWrapper.create());
    }

}
