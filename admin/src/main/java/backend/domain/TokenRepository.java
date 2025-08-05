package backend.domain;

import backend.domain.*;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "tokens", path = "tokens")
public interface TokenRepository
    extends PagingAndSortingRepository<Token, Long> {

    Optional<Token> findByUserId(Long userId);

    boolean existsByUserId(Long id);

}
