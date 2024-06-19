package io.github.yanfeiwuji.isupabase.sec;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.entity.SysRole;
import io.github.yanfeiwuji.isupabase.request.flex.PgrstContext;
import io.github.yanfeiwuji.isupabase.request.flex.anno.Policy;
import io.github.yanfeiwuji.isupabase.request.flex.policy.AllPolicyBase;

import java.util.List;

import static io.github.yanfeiwuji.isupabase.entity.table.SysRoleTableDef.SYS_ROLE;

/**
 * @author yanfeiwuji
 * @date 2024/6/19 15:59
 */
@Policy
public class SysRoleSetting extends AllPolicyBase<SysRole> {
    @Override
    public QueryCondition using(PgrstContext context) {
        return EMPTY_CONDITION;
    }

    @Override
    public List<QueryColumn> columns(PgrstContext context) {
        return List.of(SYS_ROLE.ROLE_NAME);
    }
}
