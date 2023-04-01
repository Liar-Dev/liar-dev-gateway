package liar.gateway.filter.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.gateway.domain.TokenProviderImpl;
import liar.gateway.repository.LoginSessionRepository;
import liar.gateway.repository.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Profile({"default", "local"})
@Component
public class AuthorizationDefaultConfig extends AbstractAuthorizationConfig {

    public AuthorizationDefaultConfig(TokenProviderImpl tokenProviderImpl, TokenRepository tokenRepository, LoginSessionRepository loginSessionRepository) {
        super(tokenProviderImpl, tokenRepository, loginSessionRepository);
    }

    @Override
    public boolean validateRequestHeader(String accessToken, String refreshToken, String userId) throws JsonProcessingException {
        return StringUtils.hasText(accessToken)
                && validateToken(accessToken, userId)
                && validateToken(refreshToken, userId);
    }
}
