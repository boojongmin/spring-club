package userserver.repository;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import userserver.domain.Club;

public interface ClubRepository extends ReactiveSortingRepository<Club, String> {

}
