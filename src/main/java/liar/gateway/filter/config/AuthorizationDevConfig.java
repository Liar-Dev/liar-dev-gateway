package liar.gateway.filter.config;

import liar.gateway.domain.TokenProviderImpl;
import liar.gateway.repository.LoginSessionRepository;
import liar.gateway.repository.TokenRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"dev", "prod"})
@Component
public class AuthorizationDevConfig extends AbstractAuthorizationConfig {

    public AuthorizationDevConfig(TokenProviderImpl tokenProviderImpl, TokenRepository tokenRepository, LoginSessionRepository loginSessionRepository) {
        super(tokenProviderImpl, tokenRepository, loginSessionRepository);
    }
}
