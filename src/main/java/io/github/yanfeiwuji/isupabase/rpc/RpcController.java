package io.github.yanfeiwuji.isupabase.rpc;

import io.github.yanfeiwuji.isupabase.request.anno.Rpc;
import io.github.yanfeiwuji.isupabase.request.anno.RpcMapping;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/9 14:23
 */
@RpcMapping
public class RpcController {
    @Rpc(value = "test")
    public List<String> a() {
        return List.of("test");
    }

}
