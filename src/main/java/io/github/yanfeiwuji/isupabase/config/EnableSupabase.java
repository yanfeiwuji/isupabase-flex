package io.github.yanfeiwuji.isupabase.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import java.lang.annotation.*;

/**
 * @author yanfeiwuji
 * @date 2024/6/26 17:33
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ISupaConfig.class, SecurityConfig.class})
@ComponentScan(basePackages = "io.github.yanfeiwuji.isupabase")
@MapperScan({"io.github.yanfeiwuji.isupabase.auth.mapper",
        "io.github.yanfeiwuji.isupabase.storage.mapper"})
@EnableTransactionManagement
public @interface EnableSupabase {
}
