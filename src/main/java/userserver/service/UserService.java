package userserver.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import userserver.domain.Club;
import userserver.domain.User;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final int pageSize;
    private final Sort SORT = Sort.by(Sort.Order.asc("createDate"));

    public UserService(UserRepository userRepository, ClubRepository clubRepository, @Value("${clubservice.paging-size}") int pageSize) {
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.pageSize = pageSize;
    }



    public Mono<User> get(String userId) {
        return userRepository.findById(userId);
    }

    public Mono<User> save(User user) {
        return userRepository.save(user);
    }

    public Flux<User> getList(int page) {
        return userRepository
                .findAll(SORT)
                .skip(page * pageSize)
                .limitRequest(pageSize);
    }

    public Mono<Void> delete(String userId) {
        return userRepository.findById(userId)
                .flatMap(x -> userRepository.delete(x).then());
    }

    public Mono<Club> getClub(String userId) {
        return userRepository.findById(userId)
                .map(User::getClubId)
                .flatMap(clubRepository::findById);
    }
}
