package backend.infra;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SecurityLogRepository extends MongoRepository<SecurityLog, String> {
    Optional<SecurityLog> findByUserIdAndJobId(Long userId, Long jobId);
    List<SecurityLog> findAllByUserIdOrderBySavedAtDesc(Long userId);
    Optional<SecurityLog> findByJobId(Long jobId);
}

