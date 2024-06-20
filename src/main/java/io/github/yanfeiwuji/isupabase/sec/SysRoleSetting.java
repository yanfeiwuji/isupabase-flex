package io.github.yanfeiwuji.isupabase.sec;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.entity.SysRole;
import io.github.yanfeiwuji.isupabase.request.flex.OperateInfo;
import io.github.yanfeiwuji.isupabase.request.flex.PgrstContext;
import io.github.yanfeiwuji.isupabase.request.flex.PgrstDb;
import io.github.yanfeiwuji.isupabase.request.flex.anno.Policy;
import io.github.yanfeiwuji.isupabase.request.flex.policy.AllPolicyBase;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static io.github.yanfeiwuji.isupabase.entity.table.SysRoleTableDef.SYS_ROLE;

/**
 * @author yanfeiwuji
 * @date 2024/6/19 15:59
 */
@Policy
@RequiredArgsConstructor
public class SysRoleSetting extends AllPolicyBase<SysRole> {
    private final PgrstDb pgrstDb;

    @Override
    public QueryCondition using(PgrstContext context) {
        return EMPTY_CONDITION;
    }

    @Override
    public void checking(PgrstContext context, List<SysRole> entities) {
        System.out.println(entities);
    }

    @Override
    public void before(PgrstContext context, OperateInfo<SysRole> operateInfo) {
        final QueryCondition queryCondition = operateInfo.queryCondition();
        pgrstDb.selectListByCondition(baseMapper, queryCondition);
        System.out.println("before");
    }

    @Override
    public void after(PgrstContext context, OperateInfo<SysRole> operateInfo) {
        System.out.println("after");
        System.out.println(context);
        System.out.println(operateInfo);
    }

    @Override
    public List<QueryColumn> columns(PgrstContext context) {
        return List.of(SYS_ROLE.ROLE_NAME);
    }
}
