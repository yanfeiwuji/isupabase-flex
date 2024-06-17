package io.github.yanfeiwuji.isupabase.sec;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.flex.SimpleAuthContext;
import io.github.yanfeiwuji.isupabase.flex.policy.IAllPolicy;
import org.springframework.stereotype.Service;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 18:41
 */
@Service
public class SysTableConfig implements IAllPolicy<SimpleAuthContext, SysUser> {

    @Override
    public QueryCondition using(SimpleAuthContext context) {
        return QueryCondition.createEmpty();
    }
}
