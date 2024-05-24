package io.github.yanfeiwuji.isupabase.request.select;

import java.util.List;

import com.google.common.collect.Table;
import com.mybatisflex.core.relation.AbstractRelation;
import io.github.yanfeiwuji.isupabase.flex.DepthRelQueryExt;

public record RelQueryInfo(
        Integer maxDepth,
        Table<Integer, String, String> depthRelPre,
        Table<Integer, String, DepthRelQueryExt> depthRelQueryExt,
        Table<Integer, String, AbstractRelation<?>> depthRelation,
        List<RelInner> inners, // top inner
        Table<Integer, String, List<RelInner>> depthInners
) {

}