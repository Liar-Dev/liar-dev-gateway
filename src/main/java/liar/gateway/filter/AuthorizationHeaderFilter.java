package liar.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.gateway.exception.type.ExceptionMessage;
import liar.gateway.filter.config.AuthorizationConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationConfig> {
    private final AntPathMatcher antPathMatcher;

    private static final List<String> whitelist = Arrays.asList(
            "/",
            "/static/**",
            "/favicon.ico",
            "/member-service/login",
            "/member-service/logout",
            "/member-service/register",
            "/wait-service/wait-websocket/**",
            "/wait-service/wait-websocket/info/**",
            "/wait-service/waitroom/**",
            "/wait-service/waitroom/**/join",
            "/wait-service/waitroom/**/**",
            "/oauth2/authorization/google",
            "/oauth2/authorization/naver",
            "/oauth2/authorization/kakao"
    );

    @Override
    public GatewayFilter apply(AuthorizationConfig config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            if (isWhiteList(request.getURI().getPath())) return chain.filter(exchange);
            config.validateAuthorizationHeaders(request);
            try {
                if (config.validateRequestHeader(request)) return chain.filter(exchange);
            } catch (JsonProcessingException e) {
                return onError(exchange, ExceptionMessage.BADREQUEST, HttpStatus.BAD_REQUEST);
            }
            return onError(exchange, ExceptionMessage.BADREQUEST, HttpStatus.BAD_REQUEST);
        };
    }

    /**
     * Error Log
     */
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error(err);
        return response.setComplete();
    }

    private boolean isWhiteList(String requestURI) {
        log.info("requestURI = {}", requestURI);

        return whitelist.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, requestURI));
    }

}
