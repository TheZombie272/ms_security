package com.danielflores38153.ms_security.Configurations;

import com.danielflores38153.ms_security.Interceptors.SecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private SecurityInterceptor securityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {


        registry.addInterceptor(securityInterceptor)
                .addPathPatterns("/app/**") // Si se quiere desactivar seguridad poner en vez de api cualquier cosa
                .excludePathPatterns("/api/public/**");


    }
}