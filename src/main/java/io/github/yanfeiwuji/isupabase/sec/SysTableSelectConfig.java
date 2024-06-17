package io.github.yanfeiwuji.isupabase.sec;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.flex.SimpleAuthContext;
import io.github.yanfeiwuji.isupabase.flex.policy.ISelectPolicy;
import org.springframework.stereotype.Service;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 15:39
 */
@Service
public class SysTableSelectConfig implements ISelectPolicy<SimpleAuthContext, SysUser> {
    @Override
    public QueryCondition using(SimpleAuthContext context) {
        return QueryCondition.createEmpty();
    }
}
