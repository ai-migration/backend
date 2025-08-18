package backend.infra;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConversionLogRepository extends MongoRepository<ConversionLog, String> {
    Optional<ConversionLog> findByUserIdAndJobId(Long userId, Long jobId);
    List<ConversionLog> findAllByUserIdOrderBySavedAtDesc(Long userId);
    Optional<ConversionLog> findByJobId(Long jobId);
}
