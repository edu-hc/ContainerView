package com.ftc.containerView.infra.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class SanitizationInterceptor implements HandlerInterceptor {

    @Autowired
    private InputSanitizer sanitizer;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Sanitiza query parameters
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    // Sanitiza cada parâmetro
                    values[i] = sanitizer.sanitizePlainText(values[i]);
                }
            }
        }

        // Headers sensíveis que devem ser verificados
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 500) {
            // User-Agent suspeitosamente longo
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        return true;
    }
}
