package io.github.yanfeiwuji.isupabase.request;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.table.TableInfo;
import org.springframework.web.servlet.function.ServerRequest;

@FunctionalInterface
public interface IReqQueryWrapperHandler {
    void handler(ServerRequest request, TableInfo tableInfo, QueryChain<?> queryChain);

}
