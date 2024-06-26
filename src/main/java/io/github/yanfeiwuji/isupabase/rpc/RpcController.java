package io.github.yanfeiwuji.isupabase.rpc;

import io.github.yanfeiwuji.isupabase.entity.SysRole;
import io.github.yanfeiwuji.isupabase.request.anno.Rpc;
import io.github.yanfeiwuji.isupabase.request.anno.RpcMapping;
import io.github.yanfeiwuji.isupabase.request.validate.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/9 14:23
 */
@RpcMapping
public class RpcController {
//    @Rpc("test")
//    public List<String> a() {
//        return List.of("test");
//    }
//
//    @Rpc("add")
//    public List<SysRole> add(@Validated(Valid.Update.class) @RequestBody SysRole sysRole) {
//        return List.of();
//    }
}
