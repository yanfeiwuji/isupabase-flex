package io.github.yanfeiwuji.isupabase.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.core.dialect.DbType;
import com.mybatisflex.core.dialect.DialectFactory;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;

import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.req.ApiReq;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ISupaConfig implements ConfigurationCustomizer, WebMvcConfigurer {

    @Bean
    public MyBatisFlexCustomizer myBatisFlexCustomizer() {
        return configuration -> {
            configuration.setDbType(DbType.POSTGRE_SQL);
            DialectFactory.registerDialect(DbType.POSTGRE_SQL, new AuthDialectImpl());
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

    @Bean
    CommandLineRunner commandLineRunner(ObjectMapper mapper) {
        return arg -> {
            CacheTableInfoUtils.init(mapper);
            ValueUtils.init(mapper);
            ApiReq.init(mapper);
        };
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //  WebMvcConfigurer.super.addCorsMappings(registry);
        registry.addMapping("/**")
                .allowCredentials(true)
                .maxAge(3600)
                //   .allowedHeaders(CommonStr.ALLOW_HEADERS)
                .exposedHeaders(CommonStr.EXPOSE_HEADERS)
                .allowedMethods("*")
                .allowedOriginPatterns("*");
    }
}
