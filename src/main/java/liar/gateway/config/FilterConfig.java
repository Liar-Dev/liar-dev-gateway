package liar.gateway.config;

import liar.gateway.filter.AuthorizationHeaderFilter;
import liar.gateway.filter.config.AuthorizationConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final AuthorizationConfig authorizationConfig;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, AuthorizationHeaderFilter authorizationHeaderFilter) {
        return builder.routes()

                .route("member-service", r -> r
                        .path("/member-service/users")
                        .filters(spec -> spec.filter(authorizationHeaderFilter.apply(authorizationConfig)))
                        .uri("lb://member-service"))

                .route("member-service", r -> r
                        .path("/member-service/**")
                        .uri("lb://member-service"))

                .route("wait-service", r -> r
                        .path("/wait-service/**")
                        .filters(spec -> spec.filter(authorizationHeaderFilter.apply(authorizationConfig)))
                        .uri("lb://wait-service"))

                .route("game-service", r -> r
                        .path("/game-service/**")
                        .filters(spec -> spec.filter(authorizationHeaderFilter.apply(authorizationConfig)))
                        .uri("lb://game-service"))

                .route("result-service", r -> r
                        .path("/result-service/**")
                        .filters(spec -> spec.filter(authorizationHeaderFilter.apply(authorizationConfig)))
                        .uri("lb://result-service"))


                .build();
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }
}
