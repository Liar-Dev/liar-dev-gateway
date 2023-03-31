package liar.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.gateway.domain.TokenProviderImpl;
import liar.gateway.domain.token.*;
import liar.gateway.exception.exception.NotAuthorizationRequestException;
import liar.gateway.exception.exception.NotUserIdHeaderException;
import liar.gateway.exception.type.ExceptionMessage;
import liar.gateway.repository.LoginSessionRepository;
import liar.gateway.repository.LoginSessionRepositoryImpl;
import liar.gateway.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final TokenProviderImpl tokenProviderImpl;
    private final TokenRepository tokenRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final AntPathMatcher antPathMatcher;

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESHTOKEN_HEADER = "RefreshToken";
    public static final String USER_ID_HEADER = "UserId";
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


    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            if (isWhiteList(request.getURI().getPath())) return chain.filter(exchange);
            validateAuthorizationHeaders(request);
            try {
                if (validateRequestHeader(request)) return chain.filter(exchange);
            } catch (JsonProcessingException e) {
                return onError(exchange, ExceptionMessage.BADREQUEST, HttpStatus.BAD_REQUEST);
            }
            return onError(exchange, ExceptionMessage.BADREQUEST, HttpStatus.BAD_REQUEST);
        };
    }


    /**
     * request 요청에서 token 파싱
     */
    private String parseAccessToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().get(AUTHORIZATION_HEADER).get(0);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) return bearerToken.substring(7);
        return null;
    }


    private String parseRefreshToken(ServerHttpRequest request) {
        return request.getHeaders().get(REFRESHTOKEN_HEADER).get(0);
    }

    /**
     * request 요청에서 userId 파싱
     */
    public String parseUserId(ServerHttpRequest request) {
        return request.getHeaders().get(USER_ID_HEADER).get(0);
    }

    private static void validateAuthorizationHeaders(ServerHttpRequest request) {
        hasAuthorizationHeader(request);
        hasRefreshToken(request);
        hasUserIdHeader(request);
    }

    /**
     * Authorization 헤더 포함 및 header List empty 여부 확인
     */
    private static void hasAuthorizationHeader(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION) ||
                request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).isEmpty()) {
            throw new NotAuthorizationRequestException();
        }

    }

    /**
     * userId 헤더 포함 및 header List empty 여부 확인
     */
    private static void hasUserIdHeader(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey(USER_ID_HEADER) ||
                request.getHeaders().get(USER_ID_HEADER).get(0).isEmpty()) {
            throw new NotUserIdHeaderException();
        }
    }

    /**
     * RefreshToken 헤더 포함 및 header List empty 여부 확인
     */
    private static void hasRefreshToken(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey(REFRESHTOKEN_HEADER) ||
                request.getHeaders().get(REFRESHTOKEN_HEADER).get(0).isEmpty()) {
            throw new NotAuthorizationRequestException();
        }
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

    private boolean validateRequestHeader(ServerHttpRequest request) throws JsonProcessingException {
        String accessToken = parseAccessToken(request);
        String refreshToken = parseRefreshToken(request);
        String userId = parseUserId(request);

        if (validateRequestHeader(accessToken, refreshToken, userId)) return true;
        return false;
    }

    private boolean validateRequestHeader(String accessToken, String refreshToken, String userId) throws JsonProcessingException {
        return StringUtils.hasText(accessToken)
                && tokenProviderImpl.validateToken(accessToken, userId)
                && tokenProviderImpl.validateToken(refreshToken, userId)
                && existsToken(accessToken, refreshToken)
                && isLoginSession(userId);
    }

    private boolean isWhiteList(String requestURI) {
        log.info("requestURI = {}", requestURI);

        return whitelist.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, requestURI));
    }

    private boolean existsToken(String jwt, Class<?> clazz) throws JsonProcessingException {
        Token token = tokenRepository.findTokenByKey(jwt, clazz);
        return token != null;
    }

    private boolean existsToken(String accessToken, String refreshToken) throws JsonProcessingException {
        return existsToken(accessToken, AccessToken.class) && existsToken(refreshToken, RefreshToken.class)
                && !existsToken(accessToken, LogoutSessionAccessToken.class)
                && !existsToken(refreshToken, LogoutSessionRefreshToken.class);
    }

    private boolean isLoginSession(String userId) throws JsonProcessingException {
        return loginSessionRepository.existLoginSession(userId);
    }

}
