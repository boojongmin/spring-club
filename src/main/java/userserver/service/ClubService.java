package userserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import userserver.domain.Club;
import userserver.domain.User;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;

@Service
public class ClubService {
    private ClubRepository clubRepository;
    private UserRepository userRepository;
    private final Sort SORT = Sort.by(Sort.Order.asc("createDate"));
    private final int pageSize;

    public ClubService(ClubRepository clubRepository, UserRepository userRepository, @Value("${clubservice.paging-size}") int pageSize) {
        this.clubRepository = clubRepository;
        this.userRepository = userRepository;
        this.pageSize = pageSize;
    }

    public Mono<Club> saveClub(Club club) {
        return clubRepository.save(club);
    }

    public Flux<Club> getList(int page) {
        return clubRepository
                .findAll(SORT)
                .skip(page * pageSize)
                .limitRequest(pageSize);
    }

    public Mono<Club> getClub(String id) {
        return clubRepository.findById(id);
    }

    public Mono<Void> delete(String id) {
        return clubRepository.findById(id).flatMap(x -> clubRepository.delete(x).then());
    }

//    public Mono<User> join(String clubId, String userId) {
//        Mono<User> userM = userRepository.findById(userId);
//        return userM.map(x -> {
//                x.setClubId(clubId);
//                return x;
//            })
//            .flatMap(x -> userRepository.save(x));
//    }
//
//    public Mono<Void> leave(String clubId, String userId) {
//        Mono<User> userM = userRepository.findById(userId);
//        return userM.map(x -> {
//                x.setClubId(null);
//                return x;
//            })
//            .map(x -> userRepository.save(x)).then();
//    }
}
