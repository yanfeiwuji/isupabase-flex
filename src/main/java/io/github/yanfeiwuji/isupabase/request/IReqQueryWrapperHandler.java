package io.github.yanfeiwuji.isupabase.request;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import org.springframework.web.servlet.function.ServerRequest;


@FunctionalInterface
public interface IReqQueryWrapperHandler {
    QueryWrapper handler(ServerRequest request, TableInfo tableInfo);
}
