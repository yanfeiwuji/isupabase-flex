package io.github.yanfeiwuji.isupabase.request.select;

import com.google.common.collect.Table;
import io.github.yanfeiwuji.isupabase.flex.DepthRelQueryExt;

public record RelQueryInfo(
        Integer maxDepth,
        Table<Integer, String, String> depthRelPre,
        Table<Integer, String, DepthRelQueryExt> depthRelQueryExt
) {

}
