package com.anshuman.workflow.config;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EntityScan(basePackages = {"com.anshuman.workflow.data.model.entity"})
@EnableJpaRepositories(basePackages = {"com.anshuman.workflow.data.model.repository"})
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSource() {
        return dataSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
