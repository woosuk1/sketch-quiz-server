package com.itcen.whiteboardserver.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Slf4j
public class DataSourceLogger {

    private final DataSource dataSource;

    public DataSourceLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void logJdbcUrl() {
        if (dataSource instanceof HikariDataSource hikari) {
            log.info("◆ HikariCP JDBC URL: {}", hikari.getJdbcUrl());
        } else {
            log.info("◆ DataSource is not Hikari: {}", dataSource);
        }
    }
}