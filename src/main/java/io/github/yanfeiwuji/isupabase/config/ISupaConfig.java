package io.github.yanfeiwuji.isupabase.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.core.dialect.*;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;

import io.github.yanfeiwuji.isupabase.auth.provider.AuthRequestProvider;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtils;
import io.github.yanfeiwuji.isupabase.auth.utils.DefaultAuthRequestProvider;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.request.flex.*;
import io.github.yanfeiwuji.isupabase.request.flex.policy.TableConfigUtils;
import io.github.yanfeiwuji.isupabase.request.req.ApiReq;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;

import io.github.yanfeiwuji.isupabase.stroage.provider.DefaultS3Provider;
import io.github.yanfeiwuji.isupabase.stroage.provider.S3Provider;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;


@Configuration
public class ISupaConfig implements ConfigurationCustomizer, WebMvcConfigurer {


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
        final PgrstDb pgrstDb = (PgrstDb) applicationContext.getBean(PgrstStrPool.API_REQ_PGRST_DB_BEAN);
        final Map<String, Map<OperateType, TableSetting<Object>>> settings = TableConfigUtils.toSettings(applicationContext);
        pgrstDb.load(settings);
    }

    @Bean(PgrstStrPool.API_REQ_PGRST_DB_BEAN)
    @Primary
    public PgrstDb pgrstDb(ISupabaseProperties supabaseProperties, ApplicationEventPublisher publisher) {
        return new PgrstDb(() -> new PgrstContext(AuthUtils.uid().orElse(-1L), AuthUtils.role(), AuthUtils.jwt()), supabaseProperties, publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthRequestProvider authRequestProvider() {
        return new DefaultAuthRequestProvider();
    }


    @Bean
    CommandLineRunner commandLineRunner(ObjectMapper mapper,
                                        SpringValidatorAdapter validatorAdapter,
                                        JwtEncoder jwtEncoder,
                                        PgrstDb pgrstDb) {
        return arg -> {
            CacheTableInfoUtils.init(mapper);
            ValueUtils.init(mapper);
            ApiReq.init(mapper, validatorAdapter, pgrstDb);
            JacksonTypeHandler.setObjectMapper(mapper);
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
    @ConditionalOnMissingBean
    public S3Provider s3Provider() {

        return new DefaultS3Provider();
    }


}
