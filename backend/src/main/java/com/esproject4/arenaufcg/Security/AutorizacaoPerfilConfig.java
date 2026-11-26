package com.esproject4.arenaufcg.Security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AutorizacaoPerfilConfig implements WebMvcConfigurer {

    private final AutorizacaoPerfilInterceptor interceptor;

    public AutorizacaoPerfilConfig(
            AutorizacaoPerfilInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
            .addPathPatterns("/api/v1/**");
    }
}