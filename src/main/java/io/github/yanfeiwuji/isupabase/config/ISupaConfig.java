package io.github.yanfeiwuji.isupabase.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.core.dialect.*;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;

import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.flex.AuthDialectImpl;
import io.github.yanfeiwuji.isupabase.flex.TableOneOperateConfigFor;
import io.github.yanfeiwuji.isupabase.request.req.ApiReq;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;

import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
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
import java.util.Optional;

import static io.github.yanfeiwuji.isupabase.auth.entity.table.UserTableDef.USER;

@Configuration
public class ISupaConfig implements ConfigurationCustomizer, WebMvcConfigurer {

    @Bean
    public MyBatisFlexCustomizer myBatisFlexCustomizer() {
        return configuration -> {
            // configuration.setDbType(DbType.POSTGRE_SQL);
            DialectFactory.registerDialect(DbType.MYSQL,
                    new AuthDialectImpl(KeywordWrap.BACK_QUOTE, LimitOffsetProcessor.MYSQL));
        };
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
        final List<TableOneOperateConfigFor> load = TableConfigUtils.load(applicationContext);
        System.out.println(load.size());
        AuthDialectImpl.loadRls(load);
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
