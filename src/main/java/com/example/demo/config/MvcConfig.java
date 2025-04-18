package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(MvcConfig.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            String uploadPathUri = uploadPath.toUri().toString();
            logger.info("Mapping resource handler /uploads/** to file path: {}", uploadPathUri);

            registry.addResourceHandler("/uploads/**") // URL path prefix clients will use
                    .addResourceLocations(uploadPathUri); // Actual directory on filesystem (must end with /)
        } catch (Exception e) {
            logger.error("Error configuring resource handler for uploads directory: {}", uploadDir, e);
        }
    }
}