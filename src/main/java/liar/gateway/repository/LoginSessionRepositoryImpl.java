package liar.gateway.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class LoginSessionRepositoryImpl implements LoginSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public LoginSessionRepositoryImpl(RedisTemplate<String, Object> redisTemplate,
                                      @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private final static String LOGIN_SESSION = "LoginSession:";

    @Override
    public boolean existLoginSession(String userId) {
        return redisTemplate.hasKey(getLoginSessionKey(userId));
    }

    private String getLoginSessionKey(String userId) {
        return LOGIN_SESSION + userId;
    }
}
