package com.example.expense_tracker.security;

import com.example.expense_tracker.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            jwtService.extractEmailIfValid(token)
                    .ifPresent(email -> {

                        UsernamePasswordAuthenticationToken authentication =
                                UsernamePasswordAuthenticationToken.authenticated(
                                        email,
                                        null,
                                        List.of()
                                );

                        SecurityContext context =
                                SecurityContextHolder.createEmptyContext();

                        context.setAuthentication(authentication);

                        SecurityContextHolder.setContext(context);
                    });
        }

        filterChain.doFilter(request, response);
    }
}