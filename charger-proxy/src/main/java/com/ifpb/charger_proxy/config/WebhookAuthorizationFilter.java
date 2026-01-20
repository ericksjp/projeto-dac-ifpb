package com.ifpb.charger_proxy.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class WebhookAuthorizationFilter extends OncePerRequestFilter {

    @Value("${asaas.auth-token}")
    private String webhookSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().equals("/api/webhook/asaas")) {
            String authHeader = request.getHeader("asaas-access-token");
            if (authHeader == null || !authHeader.equals(webhookSecret)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
