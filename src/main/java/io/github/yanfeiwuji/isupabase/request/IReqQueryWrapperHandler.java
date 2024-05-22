package io.github.yanfeiwuji.isupabase.request;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.req.ApiReq;
import org.springframework.web.servlet.function.ServerRequest;

@FunctionalInterface
public interface IReqQueryWrapperHandler {
    ApiReq handler(ServerRequest request, String tableName);

}
