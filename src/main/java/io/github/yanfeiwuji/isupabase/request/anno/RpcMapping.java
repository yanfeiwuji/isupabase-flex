package io.github.yanfeiwuji.isupabase.request.anno;

import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * @author yanfeiwuji
 * @date 2024/6/9 14:25
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping(path = PgrstStrPool.REST_RPC_PATH)
public @interface RpcMapping {

}
