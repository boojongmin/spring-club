package userserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import userserver.domain.Club;
import userserver.domain.User;
import userserver.enums.JoinResult;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;

import java.util.function.Function;

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

    public Mono<Club> save(Club club) {
        return clubRepository.save(club);
    }

    public Flux<Club> getList(int page) {
        return clubRepository
                .findAll(SORT)
                .skip(page * pageSize)
                .limitRequest(pageSize);
    }

    public Mono<Club> get(String id) {
        return clubRepository.findById(id);
    }

    public Mono<Void> delete(String id) {
        return clubRepository.findById(id).flatMap(x -> clubRepository.delete(x).then());
    }

    public Mono<JoinResult> join(String clubId, String userId) {
        return join(clubId, userId, this::checkJoinRule)
                .defaultIfEmpty(JoinResult.NOT_FOUND);
    }

    public <T> Mono<T> join(String clubId, String userId, Function<Tuple2<Club, User>, Mono<T>> f) {
        Mono<Club> clubM = clubRepository.findById(clubId);
        Mono<User> userM = userRepository.findById(userId);
        return Mono.zip(clubM, userM)
                .flatMap(x -> f.apply(x));
    }

    private Mono<JoinResult> checkJoinRule(Tuple2<Club, User> tuple) {
        Club club = tuple.getT1();
        User user = tuple.getT2();
        if(club == null || user == null) {
            return Mono.just(JoinResult.NOT_FOUND);
        }
        if(StringUtils.isEmpty(user.getClubId()) == false) {
            return Mono.just(JoinResult.FAIL_CLUB_IS_JOINED);
        }
        if(club.getMinAgeForJoin() > user.getAge()) {
            return Mono.just(JoinResult.FAIL_NOT_ALLOW_AGE );
        }
        user.setClubId(club.getId());
        return userRepository.save(user)
                .then(clubRepository.save(club))
                .then(Mono.just(JoinResult.SUCCESS));
    }

     public Mono<User> leave(String userId) {
        return userRepository.findById(userId)
                .flatMap(x -> {
                    x.setClubId(null);
                    return userRepository.save(x);
                });
    }
}
