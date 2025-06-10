package com.itcen.whiteboardserver.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceLogger {

    private final DataSource dataSource;

    public DataSourceLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void logJdbcUrl() {
        if (dataSource instanceof HikariDataSource hikari) {
            System.out.println("◆ HikariCP JDBC URL: " + hikari.getJdbcUrl());
        } else {
            System.out.println("◆ DataSource is not Hikari: " + dataSource);
        }
    }
}