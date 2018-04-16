package userserver.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Mono;
import userserver.domain.Club;
import userserver.domain.User;

public interface ClubRepository extends ReactiveSortingRepository<Club, String> {
//    @Query(value="{ 'users.id': ?0 }")
    // TODO 원하는User만db에서걸러서가져오고싶은데잘안됨.
//    @Query(value="{ 'users.id': {'$eq': ?0} }")
    @Query(value="{ 'users.id': {'$eq': ?0} }", fields = "{ 'users.id': 1}")
    Mono<Club> findByUserId(String userId);

//    @Query(value="{ 'users.id': {'$eq': ?0} }")
    Mono<Integer> countByUsersId(String usersId);
}
