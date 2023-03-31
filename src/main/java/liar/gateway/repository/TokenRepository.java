package liar.gateway.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.gateway.domain.token.Token;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository<T extends Token> {

    T findTokenByKey(String key, Class<T> clazz) throws JsonProcessingException;

    String findTokenIdxValue(String key, Class<T> clazz) throws JsonProcessingException;
}