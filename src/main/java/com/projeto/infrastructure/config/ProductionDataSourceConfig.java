package com.projeto.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
@Profile("prod")
public class ProductionDataSourceConfig {

    @Bean
    public DataSource dataSource(Environment environment) {
        DatabaseConnection connection = resolveConnection(environment);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(connection.jdbcUrl());

        if (StringUtils.hasText(connection.username())) {
            dataSource.setUsername(connection.username());
        }

        if (StringUtils.hasText(connection.password())) {
            dataSource.setPassword(connection.password());
        }

        return dataSource;
    }

    private DatabaseConnection resolveConnection(Environment environment) {
        String rawUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("SPRING_DATASOURCE_URL")
        );

        if (!StringUtils.hasText(rawUrl)) {
            throw new IllegalStateException("DATABASE_URL ou SPRING_DATASOURCE_URL deve ser configurada no ambiente de producao");
        }

        URI uri = parseUri(rawUrl);

        String jdbcUrl = normalizeJdbcUrl(rawUrl, uri);
        String username = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_USERNAME"),
                environment.getProperty("DB_USERNAME"),
                extractUserInfo(uri, 0)
        );
        String password = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_PASSWORD"),
                environment.getProperty("DB_PASSWORD"),
                extractUserInfo(uri, 1)
        );

        return new DatabaseConnection(jdbcUrl, username, password);
    }

    private String normalizeJdbcUrl(String rawUrl, URI uri) {
        if (rawUrl.startsWith("jdbc:")) {
            return rawUrl;
        }

        if (uri == null) {
            return rawUrl;
        }

        if (!"postgres".equalsIgnoreCase(uri.getScheme()) && !"postgresql".equalsIgnoreCase(uri.getScheme())) {
            return rawUrl;
        }

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
        if (StringUtils.hasText(uri.getHost())) {
            jdbcUrl.append(uri.getHost());
        }
        if (uri.getPort() > 0) {
            jdbcUrl.append(":").append(uri.getPort());
        }
        if (StringUtils.hasText(uri.getPath())) {
            jdbcUrl.append(uri.getPath());
        }
        if (StringUtils.hasText(uri.getQuery())) {
            jdbcUrl.append("?").append(uri.getQuery());
        }

        return jdbcUrl.toString();
    }

    private URI parseUri(String rawUrl) {
        try {
            return StringUtils.hasText(rawUrl) ? URI.create(rawUrl) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String extractUserInfo(URI uri, int index) {
        if (uri == null || !StringUtils.hasText(uri.getUserInfo())) {
            return null;
        }

        String[] userInfo = uri.getUserInfo().split(":", 2);
        if (userInfo.length <= index) {
            return null;
        }

        return userInfo[index];
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }

        return null;
    }

    private record DatabaseConnection(String jdbcUrl, String username, String password) {
    }
}