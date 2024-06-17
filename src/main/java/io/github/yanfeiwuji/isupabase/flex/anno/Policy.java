package io.github.yanfeiwuji.isupabase.flex.anno;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:30
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Policy {
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}
