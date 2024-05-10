package io.github.yanfeiwuji.isupabase.request;

import org.springframework.web.servlet.function.ServerRequest;

public interface IBodyHandler {
    <T> BodyInfo<T> handler(ServerRequest request, Class<T> entityClass);
}
