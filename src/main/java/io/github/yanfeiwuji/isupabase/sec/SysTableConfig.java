package io.github.yanfeiwuji.isupabase.sec;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.request.flex.OperateInfo;
import io.github.yanfeiwuji.isupabase.request.flex.SimpleAuthContext;
import io.github.yanfeiwuji.isupabase.request.flex.policy.AllPolicyBase;
import org.springframework.stereotype.Service;

import static io.github.yanfeiwuji.isupabase.entity.table.SysUserTableDef.SYS_USER;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 18:41
 */
@Service
public class SysTableConfig extends AllPolicyBase<SimpleAuthContext, SysUser> {

    @Override
    public QueryCondition using(SimpleAuthContext context) {

        return SYS_USER.AGE.lt(1);
    }




    @Override
    public void before(SimpleAuthContext context, OperateInfo<SysUser> operateInfo) {
        final QueryCondition queryCondition = operateInfo.queryCondition();
        System.out.println(queryCondition+"--");
        operateInfo.objects().forEach(it -> {
            System.out.println(it);
        });
        super.before(context, operateInfo);
    }
}
