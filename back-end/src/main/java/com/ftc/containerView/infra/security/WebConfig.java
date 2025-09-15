package com.ftc.containerView.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SanitizationInterceptor sanitizationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Aplica o interceptador em todas as rotas exceto recursos estáticos
        registry.addInterceptor(sanitizationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/h2-console/**",
                        "/actuator/**"
                );
    }
}
