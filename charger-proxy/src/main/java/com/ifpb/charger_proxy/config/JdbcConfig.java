package com.ifpb.charger_proxy.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;

import com.ifpb.charger_proxy.infra.jdbc.JsonbToMapConverter;
import com.ifpb.charger_proxy.infra.jdbc.MapToJsonbConverter;

@Configuration
public class JdbcConfig {

    @Bean
    JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(new MapToJsonbConverter(), new JsonbToMapConverter()));
    }
}
