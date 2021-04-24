package com.example.jpa.sharding.demo.core.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceUnwrapper;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class GoldJdbcConfig {
    @Bean("hikariConfig")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig hikariConfig(){
        return new HikariConfig();
    }
    @Bean("dataSourceMap")
    public Map<String, DataSource> dataSourceMap(DataSourceProperties properties,HikariConfig hikariConfig) {
        hikariConfig.setDriverClassName(properties.determineDriverClassName());
        hikariConfig.setJdbcUrl(properties.getUrl());
        hikariConfig.setUsername(properties.getUsername());
        hikariConfig.setPassword(properties.getPassword());
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
        HikariDataSource result = new HikariDataSource(hikariConfig);

        if (StringUtils.hasText(properties.getName())) {
            result.setPoolName(properties.getName());
        }
        dataSourceMap.put("ds0",result);
        return dataSourceMap;

    }
    /**
     * 我们把我们的数据源，给sharding jdbc使用，并且还给了DataSourcePoolMetadataProvider使用，用来提供一些metric的监控指标
     */
    @Bean(name = "hikariPoolDataSourceMetadataProviderSharding")
    @Primary
    DataSourcePoolMetadataProvider hikariPoolDataSourceMetadataProvider(@Qualifier("dataSourceMap") Map<String, DataSource> dataSourceMap ) {
        return (dataSource) -> {
            HikariDataSource hikariDataSource = DataSourceUnwrapper.unwrap(dataSourceMap.get("ds0"), HikariConfigMXBean.class,
                    HikariDataSource.class);
            if (hikariDataSource != null) {
                return new HikariDataSourcePoolMetadata(hikariDataSource);
            }
            return null;
        };
    }

    @Bean
    @Primary //由于数据源可能有多个，我们以这个为准
    public DataSource dataSource(@Qualifier("dataSourceMap") Map<String, DataSource> dataSourceMap) throws SQLException {
        // 配置真实数据源，由于我们只是分表，所以只需要配置一个数据源即可
//        Map<String, DataSource> dataSourceMap = new HashMap<>();
//        dataSourceMap.put("ds0", dataSourceMap.get());

        // 配置Order表规则,利用groovy语法，我们把1-256转化成16进制表示，这里就是UUID的前两位。
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration("user_orders","ds0.user_orders${(1..3).collect{e->Integer.toHexString(0x1000 | e).substring(2)}}");

        // 配置分库 + 分表策略
        orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("uuid", "ds0"));
        orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("uuid", "user_orders${uuid.substring(0,2)}"));

        // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);

        // 获取数据源对象
        DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new Properties());
        return dataSource;
    }
}
