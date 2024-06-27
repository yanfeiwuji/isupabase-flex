package io.github.yanfeiwuji.isupabase.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.mybatisflex.core.dialect.*;
import com.mybatisflex.core.handler.JacksonTypeHandler;

import io.github.yanfeiwuji.isupabase.auth.provider.AuthMimeMessagePreparationProvider;
import io.github.yanfeiwuji.isupabase.auth.provider.AuthRequestProvider;
import io.github.yanfeiwuji.isupabase.auth.provider.DefaultAuthMimeMessagePreparationProvider;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtils;
import io.github.yanfeiwuji.isupabase.auth.provider.DefaultAuthRequestProvider;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.request.IReqHandler;
import io.github.yanfeiwuji.isupabase.request.flex.*;
import io.github.yanfeiwuji.isupabase.request.flex.policy.TableConfigUtils;
import io.github.yanfeiwuji.isupabase.request.req.ApiReq;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;

import io.github.yanfeiwuji.isupabase.storage.provider.DefaultS3Provider;
import io.github.yanfeiwuji.isupabase.storage.provider.S3Provider;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Instant;
import java.util.Map;

@Slf4j
@EnableWebSecurity
@Configuration
public class ISupaConfig implements WebMvcConfigurer {


    @Bean
    RouterFunction<ServerResponse> routerFunction(IReqHandler reqHandler) {
        return reqHandler.routerFunction();
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
    @ConditionalOnMissingBean
    @ConditionalOnProperty("spring.mail.username")
    public AuthMimeMessagePreparationProvider authMimeMessagePreparationProvider(MailProperties mailProperties) {
        return new DefaultAuthMimeMessagePreparationProvider(mailProperties.getUsername());
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
        log.info("JWT token: {}", encode.getTokenValue());
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Provider<Resource> s3Provider() {
        return new DefaultS3Provider();
    }


    @Bean
    Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            builder.propertyNamingStrategy(SnakeCaseStrategy.INSTANCE);
        };
    }


}
