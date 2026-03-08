package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.rest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-Api-Key";

    private final String expectedApiKey;

    public ApiKeyFilter(String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (!expectedApiKey.equals(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or missing API key\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
