package liar.gateway.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.gateway.domain.session.LoginSession;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginSessionRepository {
    boolean existLoginSession(String userId);
}
