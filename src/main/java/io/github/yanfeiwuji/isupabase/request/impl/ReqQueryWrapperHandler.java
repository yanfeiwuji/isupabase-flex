package io.github.yanfeiwuji.isupabase.request.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;

@Component
public class ReqQueryWrapperHandler implements IReqQueryWrapperHandler {

    @Override
    public QueryWrapper handler(ServerRequest request, TableInfo tableInfo) {
        return QueryWrapper.create();
    }
}
