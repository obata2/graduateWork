package com.gwork.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// --- グローバルなCORS設定 --- 
@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
      .allowedOriginPatterns("*")
      .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
      .allowedHeaders("*")
      .exposedHeaders("Authorization")
      .allowCredentials(true)
      .maxAge(3600);
  }
}