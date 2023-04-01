package liar.gateway.filter.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.gateway.domain.TokenProviderImpl;
import liar.gateway.domain.token.*;
import liar.gateway.exception.exception.NotAuthorizationRequestException;
import liar.gateway.exception.exception.NotUserIdHeaderException;
import liar.gateway.repository.LoginSessionRepository;
import liar.gateway.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class AbstractAuthorizationConfig implements AuthorizationConfig {

    private final TokenProviderImpl tokenProviderImpl;
    private final TokenRepository tokenRepository;
    private final LoginSessionRepository loginSessionRepository;

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESHTOKEN_HEADER = "RefreshToken";
    public static final String USER_ID_HEADER = "UserId";

    @Override
    public void validateAuthorizationHeaders(ServerHttpRequest request) {
        hasAuthorizationHeader(request);
        hasRefreshToken(request);
        hasUserIdHeader(request);
    }

    @Override
    public boolean validateRequestHeader(ServerHttpRequest request) throws JsonProcessingException {
        String accessToken = parseAccessToken(request);
        String refreshToken = parseRefreshToken(request);
        String userId = parseUserId(request);

        if (validateRequestHeader(accessToken, refreshToken, userId)) return true;
        return false;
    }

    @Override
    public boolean validateRequestHeader(String accessToken, String refreshToken, String userId) throws JsonProcessingException {
        log.info("this is dev");
        return StringUtils.hasText(accessToken)
                && validateToken(accessToken, userId)
                && validateToken(refreshToken, userId)
                && existsToken(accessToken, refreshToken)
                && isLoginSession(userId);
    }

    protected void hasAuthorizationHeader(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION) ||
                request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).isEmpty()) {
            throw new NotAuthorizationRequestException();
        }

    }

    /**
     * userId 헤더 포함 및 header List empty 여부 확인
     */
    protected void hasUserIdHeader(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey(USER_ID_HEADER) ||
                request.getHeaders().get(USER_ID_HEADER).get(0).isEmpty()) {
            throw new NotUserIdHeaderException();
        }
    }

    /**
     * RefreshToken 헤더 포함 및 header List empty 여부 확인
     */
    protected void hasRefreshToken(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey(REFRESHTOKEN_HEADER) ||
                request.getHeaders().get(REFRESHTOKEN_HEADER).get(0).isEmpty()) {
            throw new NotAuthorizationRequestException();
        }
    }


    /**
     * request 요청에서 token 파싱
     */
    protected String parseAccessToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().get(AUTHORIZATION_HEADER).get(0);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) return bearerToken.substring(7);
        return null;
    }


    protected String parseRefreshToken(ServerHttpRequest request) {
        return request.getHeaders().get(REFRESHTOKEN_HEADER).get(0);
    }

    /**
     * request 요청에서 userId 파싱
     */
    protected String parseUserId(ServerHttpRequest request) {
        return request.getHeaders().get(USER_ID_HEADER).get(0);
    }

    protected boolean existsToken(String jwt, Class<?> clazz) throws JsonProcessingException {
        Token token = tokenRepository.findTokenByKey(jwt, clazz);
        return token != null;
    }

    protected boolean existsToken(String accessToken, String refreshToken) throws JsonProcessingException {
        return existsToken(accessToken, AccessToken.class) && existsToken(refreshToken, RefreshToken.class)
                && !existsToken(accessToken, LogoutSessionAccessToken.class)
                && !existsToken(refreshToken, LogoutSessionRefreshToken.class);
    }

    protected boolean isLoginSession(String userId) throws JsonProcessingException {
        return loginSessionRepository.existLoginSession(userId);
    }

    protected boolean validateToken(String token, String userId) {
        return tokenProviderImpl.validateToken(token, userId);
    }

}
