package userserver.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import userserver.domain.Club;
import userserver.repository.ClubRepository;

@Service
public class ClubService {
    @NonNull private ClubRepository repository;
    private final Sort SORT = Sort.by(Sort.Order.asc("createDate"));
    private final int pageSize;

    public ClubService(ClubRepository repository, @Value("${clubservice.paging-size}") int pageSize) {
        this.repository = repository;
        this.pageSize = pageSize;
    }

    public Mono<Club> saveClub(Club club) {
        return repository.save(club);
    }

    public Flux<Club> getList(int page) {
        return repository
                .findAll(SORT)
                .skip(page * pageSize)
                .limitRequest(pageSize);
    }

    public Mono<Club> getClub(String id) {
        return repository.findById(id);
    }

    public Mono<Void> delete(String id) {
        return repository.findById(id).flatMap(x -> repository.delete(x).then());
    }
}
