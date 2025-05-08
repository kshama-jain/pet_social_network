package com.petconnect.petsocial.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Register resource handler for uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/"); // Points to uploads directory in the project root
    }
}