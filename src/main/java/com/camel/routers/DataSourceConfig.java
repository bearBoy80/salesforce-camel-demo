package com.camel.routers;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @ClassName：DataSourceConfig
 * @Description：TODO
 * @Author：db.xie
 * @Date：2022/11/3 13:59
 * @Version：1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource salesforce() {
        return DruidDataSourceBuilder.create().build();
    }

}
