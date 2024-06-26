package io.github.yanfeiwuji.isupabase.auth.security;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.auth.entity.AuthBase;
import io.github.yanfeiwuji.isupabase.request.flex.PgrstContext;
import io.github.yanfeiwuji.isupabase.request.flex.anno.Policy;
import io.github.yanfeiwuji.isupabase.request.flex.policy.AllPolicyBase;

import java.util.List;

/**
 * not op auth info
 *
 * @author yanfeiwuji
 * @date 2024/6/26 16:44
 */
@Policy
public class AuthPolicy extends AllPolicyBase<AuthBase> {

    @Override
    public QueryCondition using(PgrstContext context) {
        throw deniedOnRowEx();
    }

    @Override
    public void checking(PgrstContext context, List<AuthBase> entities) {
        throw deniedOnRowEx();
    }
}
