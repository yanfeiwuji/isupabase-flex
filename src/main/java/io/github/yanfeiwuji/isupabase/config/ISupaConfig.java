package io.github.yanfeiwuji.isupabase.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.core.dialect.*;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;

import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtils;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.flex.AuthContextSupplier;
import io.github.yanfeiwuji.isupabase.flex.AuthDialectImpl;
import io.github.yanfeiwuji.isupabase.flex.SimpleAuthContext;
import io.github.yanfeiwuji.isupabase.flex.policy.TableConfigUtils;
import io.github.yanfeiwuji.isupabase.request.req.ApiReq;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;

import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.util.List;


@Configuration
public class ISupaConfig implements ConfigurationCustomizer, WebMvcConfigurer {

    @Bean
    public MyBatisFlexCustomizer myBatisFlexCustomizer(
            AuthDialectImpl<SimpleAuthContext> dialect
    ) {

        return configuration -> DialectFactory.registerDialect(DbType.MYSQL, dialect);
    }

    @Bean
    public AuthDialectImpl<SimpleAuthContext> authDialect(AuthContextSupplier<SimpleAuthContext> authContextSupplier) {
        return new AuthDialectImpl<>(KeywordWrap.BACK_QUOTE, LimitOffsetProcessor.MYSQL, authContextSupplier);
    }

    @Bean
    public AuthContextSupplier<SimpleAuthContext> simpleAuthContextSupplier() {
        return () -> new SimpleAuthContext(AuthUtils.uid().orElse(-1L), AuthUtils.role());
    }

    @Override
    public void customize(FlexConfiguration flexConfiguration) {
        AuditManager.setAuditEnable(true);
        MessageCollector collector = new ConsoleMessageCollector();
        AuditManager.setMessageCollector(collector);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
        };
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        final ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        final AuthDialectImpl authDialect = applicationContext.getBean(AuthDialectImpl.class);
        authDialect.init(TableConfigUtils.load(applicationContext));

    }

    @Bean
    CommandLineRunner commandLineRunner(ObjectMapper mapper,
                                        SpringValidatorAdapter validatorAdapter,
                                        JwtEncoder jwtEncoder) {
        return arg -> {
            CacheTableInfoUtils.init(mapper);
            ValueUtils.init(mapper);
            ApiReq.init(mapper, validatorAdapter);
            printAnnoToken(jwtEncoder);


        };
    }

    @Override
    public void addCorsMappings(@SuppressWarnings("null") CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .maxAge(3600)
                .exposedHeaders(PgrstStrPool.EXPOSE_HEADERS)
                .allowedMethods("*")
                .allowedOriginPatterns("*");
    }

    public void printAnnoToken(JwtEncoder jwtEncoder) {
        final JwtEncoderParameters parameters = JwtEncoderParameters.from(JwtClaimsSet.builder()
                .issuer("yanfeiwuji")
                .claim("ref", "")
                .claim("role", "anon")
                .issuedAt(Instant.EPOCH)
                .expiresAt(Instant.EPOCH.plusSeconds(100L * 365 * 24 * 60 * 60))
                .build());
        final Jwt encode = jwtEncoder.encode(parameters);
        System.out.println(encode.getTokenValue());

    }

    @Bean
    public AuthStateCache authStateCache() {
        return AuthDefaultStateCache.INSTANCE;
    }
}
