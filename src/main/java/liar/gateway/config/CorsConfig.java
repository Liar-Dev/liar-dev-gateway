package liar.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOriginPattern("http://localhost:8000");
        corsConfiguration.addAllowedOriginPattern("http://localhost:3000");
        corsConfiguration.addAllowedOriginPattern("http://43.201.26.3:8080");
        corsConfiguration.addAllowedOriginPattern("http://10.50.71.67:8000");
        corsConfiguration.addAllowedOriginPattern("http://3.39.187.39:8080");
        corsConfiguration.addAllowedOriginPattern("http://3.39.187.39:8761");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addExposedHeader("Access-Control-Allow-Origin");
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }

}
