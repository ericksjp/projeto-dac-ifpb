package com.ifpb.charger_proxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final WebhookAuthorizationFilter webhookAuthorizationFilter;

    public SecurityConfig(WebhookAuthorizationFilter webhookAuthorizationFilter) {
        this.webhookAuthorizationFilter = webhookAuthorizationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
            .addFilterBefore(webhookAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
