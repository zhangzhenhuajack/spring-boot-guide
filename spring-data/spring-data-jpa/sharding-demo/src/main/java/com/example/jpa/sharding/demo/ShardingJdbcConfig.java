package com.example.jpa.sharding.demo;

import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceUnwrapper;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
//@AutoConfigureBefore(HibernateJpaAutoConfiguration.class)
@EnableConfigurationProperties(value = DataSourceProperties.class)
public class ShardingJdbcConfig {
    /**
     * 当我们把hikari数据源改成 shardingJdbc的数据源之后，会出现spring boot里面关于hikari数据源的监控指标失效的问题，所以我们把数据源再单独拎出来一下
     * @param properties
     * @return
     */
    @Bean(name = "hikariDataSource1")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource1(DataSourceProperties properties){
        HikariDataSource dataSource1 = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if (StringUtils.hasText(properties.getName())) {
            dataSource1.setPoolName(properties.getName());
        }
        return dataSource1;
    }

    @Primary //由于数据源可能有多个，我们以这个为准
    @Bean
    public DataSource dataSource(@Qualifier("hikariDataSource1") HikariDataSource dataSource1) throws SQLException {
        // 配置真实数据源，由于我们只是分表，所以只需要配置一个数据源即可
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds0", dataSource1);

        // 配置Order表规则,利用groovy语法，我们把1-256转化成16进制表示，这里就是UUID的前两位。
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration("user_orders","ds0.user_orders${(1..256).collect{e->Integer.toHexString(e)}}");

        // 配置分库 + 分表策略
        orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("uuid", "ds0"));
        orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("uuid", "user_orders${Integer.parseInt(name.substring(0,1),16)}"));

        // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);

        // 获取数据源对象
        DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new Properties());
        return dataSource;
    }

    /**
     * 我们把我们的数据源，给sharding jdbc使用，并且还给了DataSourcePoolMetadataProvider使用，用来提供一些metric的监控指标
     * @param dataSource1
     * @return
     */
    @Bean(name = "hikariPoolDataSourceMetadataProviderSharding")
    @Primary
    DataSourcePoolMetadataProvider hikariPoolDataSourceMetadataProvider(@Qualifier("hikariDataSource1") HikariDataSource dataSource1) {
        return (dataSource) -> {
            HikariDataSource hikariDataSource = DataSourceUnwrapper.unwrap(dataSource1, HikariConfigMXBean.class,
                    HikariDataSource.class);
            if (hikariDataSource != null) {
                return new HikariDataSourcePoolMetadata(hikariDataSource);
            }
            return null;
        };
    }
}
