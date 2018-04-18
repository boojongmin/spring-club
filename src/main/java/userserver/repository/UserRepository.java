package userserver.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import userserver.domain.User;

import java.util.Scanner;

public interface UserRepository extends ReactiveSortingRepository<User, String> {
//    Flux<User> findByClubId(String id);
}
