package backend.infra;

import backend.domain.*;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(
    collectionResourceRel = "postReadModels",
    path = "postReadModels"
)
public interface PostReadModelRepository
    extends PagingAndSortingRepository<PostReadModel, Long> {

    Optional<PostReadModel> findByPostId(Long postId);
    List<PostReadModel> findByType(PostType type);
    }
