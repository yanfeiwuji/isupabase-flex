package io.github.yanfeiwuji.isupabase.request.req;

import com.mybatisflex.core.query.*;
import com.mybatisflex.core.relation.AbstractRelation;
import io.github.yanfeiwuji.isupabase.flex.DepthRelQueryExt;
import io.github.yanfeiwuji.isupabase.request.utils.RelationUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RelInnerHandler {
    public void handlerRelInner(AbstractRelation<?> relation, QueryWrapper queryWrapper, DepthRelQueryExt depthRelQueryExt) {

        QueryWrapper existQueryWrapper = RelationUtils.relationExistQueryWrapper(relation);
        existQueryWrapper.and(depthRelQueryExt.getCondition());
        queryWrapper.and(QueryMethods.exists(existQueryWrapper));
    }
}
