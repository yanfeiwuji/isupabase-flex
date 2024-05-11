package io.github.yanfeiwuji.isupabase.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mybatisflex.core.dialect.DbType;
import com.mybatisflex.core.dialect.DialectFactory;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;

import io.github.yanfeiwuji.isupabase.request.utils.TableInfoCache;

import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationStartupAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.metrics.ApplicationStartup;

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
        flexConfiguration.setLogImpl(StdOutImpl.class);
        // flexConfiguration.setDo
    }

    @Bean
    CommandLineRunner commandLineRunner(ObjectMapper mapper) {
        return (arg) -> TableInfoCache.init(mapper);
    }

}
