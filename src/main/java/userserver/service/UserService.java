package userserver.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import userserver.domain.User;
import userserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    public UserService(UserRepository repository, @Value("${clubservice.paging-size}") int pageSize) {
        this.repository = repository;
        this.pageSize = pageSize;
    }

    @NonNull private UserRepository repository;
    private final int pageSize;
    private final Sort SORT = Sort.by(Sort.Order.asc("createDate"));

    public Mono<User> getUser(String id) {
        return repository.findById(id);
    }

    public Mono<User> save(User user) {
        return repository.save(user);
    }

    public Flux<User> getList(int page) {
        return repository
                .findAll(SORT)
                .skip(page * pageSize)
                .limitRequest(pageSize);
    }

    public Mono<Void> delete(String id) {
        return repository.findById(id)
                .flatMap(x -> repository.delete(x).then());
    }
}
