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

import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;

import io.github.yanfeiwuji.isupabase.request.utils.ExchangeUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        //  flexConfiguration.setLogImpl(StdOutImpl.class);
        // flexConfiguration.setDo

        AuditManager.setAuditEnable(true);

        //设置 SQL 审计收集器
        MessageCollector collector = new ConsoleMessageCollector();
        AuditManager.setMessageCollector(collector);
    }

    @Bean
    CommandLineRunner commandLineRunner(ObjectMapper mapper) {
        return arg -> {
            CacheTableInfoUtils.init(mapper);
            ExchangeUtils.init(mapper);
            ValueUtils.init(mapper);
        };
    }


}
