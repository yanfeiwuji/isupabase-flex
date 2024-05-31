package io.github.yanfeiwuji.isupabase.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.core.dialect.DbType;
import com.mybatisflex.core.dialect.DialectFactory;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;

import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Set;

@Configuration
public class ISupaConfig implements ConfigurationCustomizer {

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
            ObjectMapper objectMapper = new ObjectMapper();
            builder.configure(objectMapper);

            // before use a old objectMapper that  don't  use Object ser handler
            // then  builder gen ObjectMapper use Object gen to handler cycle ser object
            builder.serializerByType(Object.class, new PgrstJsonSerializer(objectMapper));
        };
    }

    @Bean
    CommandLineRunner commandLineRunner(ObjectMapper mapper) {
        return arg -> {
            CacheTableInfoUtils.init(mapper);
            ValueUtils.init(mapper);
        };
    }

}
