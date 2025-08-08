package backend.infra;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConversionLogRepository extends MongoRepository<ConversionLog, String> {
    Optional<ConversionLog> findByJobIdAndUserId(Long userId, Long jobId);
}
