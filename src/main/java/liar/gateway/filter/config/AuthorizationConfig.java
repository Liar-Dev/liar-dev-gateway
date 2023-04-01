package liar.gateway.filter.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.server.reactive.ServerHttpRequest;

public interface AuthorizationConfig {

    void validateAuthorizationHeaders(ServerHttpRequest request);
    boolean validateRequestHeader(ServerHttpRequest request) throws JsonProcessingException;
    boolean validateRequestHeader(String accessToken, String refreshToken, String userId) throws JsonProcessingException;

}
