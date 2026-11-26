package com.esproject4.arenaufcg.Security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.esproject4.arenaufcg.Service.AuthService;

import jakarta.servlet.DispatcherType;

@Configuration
@ConditionalOnWebApplication(
    type = ConditionalOnWebApplication.Type.SERVLET
)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthService authService) throws Exception {

        var matcher = PathPatternRequestMatcher.withDefaults();

        RequestMatcher rotasPublicas = new OrRequestMatcher(
            matcher.matcher(HttpMethod.POST, "/api/v1/auth/register"),
            matcher.matcher(HttpMethod.POST, "/api/v1/auth/login"),
            matcher.matcher(HttpMethod.POST, "/api/v1/auth/verify"),
            matcher.matcher(HttpMethod.POST, "/api/v1/auth/forgot-password"),
            matcher.matcher(HttpMethod.POST, "/api/v1/auth/reset-password/{token}")
        );

        return http
            .csrf(csrf -> csrf.disable())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, ex) ->
                    JwtAuthenticationFilter.responderNaoAutenticado(response)
                )
            )

            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(rotasPublicas).permitAll()
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                new JwtAuthenticationFilter(authService, rotasPublicas),
                UsernamePasswordAuthenticationFilter.class
            )

            .build();
    }
}