package io.github.yanfeiwuji.isupabase;

import org.apache.ibatis.logging.LogFactory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

/**
 * @author yanfeiwuji
 * @date 2024/6/6 18:23
 */
@Reflective
@RegisterReflectionForBinding
@Configuration
public class A implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        hints.resources().registerType(LogFactory.class);

    }
}
