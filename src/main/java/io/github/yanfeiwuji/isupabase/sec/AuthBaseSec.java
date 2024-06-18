package io.github.yanfeiwuji.isupabase.sec;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.auth.entity.AuthBase;
import io.github.yanfeiwuji.isupabase.request.flex.SimpleAuthContext;
import io.github.yanfeiwuji.isupabase.request.flex.anno.Policy;
import io.github.yanfeiwuji.isupabase.request.flex.policy.AllPolicyBase;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 16:51
 */
@Policy
public class AuthBaseSec extends AllPolicyBase<SimpleAuthContext, AuthBase> {
    @Override
    public QueryCondition using(SimpleAuthContext context) {
        throw new RuntimeException("xxxx");

    }
}
