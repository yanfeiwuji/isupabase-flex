package io.github.yanfeiwuji.isupabase.sec;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.entity.table.SysUserTableDef;
import io.github.yanfeiwuji.isupabase.flex.ITableOneOperate;
import io.github.yanfeiwuji.isupabase.flex.InvokeContext;
import org.springframework.stereotype.Service;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 18:41
 */
@Service
public class SysTableConfig implements ITableOneOperate<SysUser> {
    @Override
    public QueryCondition using(InvokeContext context) {
        System.out.println("11");
        // todo
        return SysUserTableDef.SYS_USER.AGE.gt(100);
    }
}
