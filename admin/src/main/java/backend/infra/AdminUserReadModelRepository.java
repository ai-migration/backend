package backend.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.domain.AdminUserReadModel;

public interface AdminUserReadModelRepository extends JpaRepository<AdminUserReadModel, Long> {
}
