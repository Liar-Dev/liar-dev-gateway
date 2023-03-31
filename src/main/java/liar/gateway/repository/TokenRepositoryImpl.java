package liar.gateway.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import liar.gateway.domain.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

@Slf4j
@Repository
public class TokenRepositoryImpl<T extends Token> implements TokenRepository<Token> {
    private final RedisTemplate<String, Object> redisTemplate;

    @Qualifier("redisObjectMapper")
    private final ObjectMapper objectMapper;

    public TokenRepositoryImpl(RedisTemplate<String, Object> redisTemplate,
                               @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private final static String IDX = "idx";

    @Override
    public Token findTokenByKey(String key, Class<Token> clazz) throws JsonProcessingException {
        key = getKey(clazz.getSimpleName(), key);
        return getObjectValue(key, clazz);
    }

    @Override
    public String findTokenIdxValue(String key, Class<Token> clazz) throws JsonProcessingException {
        key = getKey(clazz.getSimpleName(), key, IDX);
        return getIdxValue(key);
    }

    private Token getObjectValue(String key, Class<Token> clazz) throws JsonProcessingException {
        String value = (String) redisTemplate.opsForValue().get(key);
        if (value == null || value.isEmpty()) return null;
        return objectMapper.readValue(value, clazz);
    }

    private String getKey(String... keys) {
        return String.join(":", keys);
    }

    private String getIdxValue(String key) throws JsonProcessingException {
        String value = (String) redisTemplate.opsForValue().get(key);
        if (ObjectUtils.isEmpty(value)) return null;
        return objectMapper.readValue(value, String.class);
    }
}