package userserver.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import userserver.domain.Club;
import userserver.domain.User;
import userserver.model.TestModelFactory;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;
import userserver.util.DomainUtil;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
@TestPropertySource("classpath:application.properties")
public class ClubJoinServiceTest {
    @Autowired ClubRepository clubRepository;
    @Autowired ReactiveMongoTemplate mongoTemplate;
    @Autowired UserRepository userRepository;
    @Value("${clubservice.paging-size}") int pageSize;
    ClubService clubService;

    @Before
    public void setUp() {
        this.clubService = new ClubService(clubRepository, userRepository, pageSize);
        clubRepository.deleteAll().block();
        userRepository.deleteAll().block();
    }

    @Test
    public void createClub() {
        Club club = clubService.saveClub(TestModelFactory.createClub()).block();
        assertThat(club.getId()).isNotBlank();
    }

    @Test
    public void getList() {
        DomainUtil.createClubs(clubRepository);
        long count = clubService.getList(0)
                .toStream()
                .count();
        assertThat(count).isEqualTo(pageSize);
    }

    @Test
    public void getUser() {
        Club club = DomainUtil.createClub(clubRepository);
        Club result = clubService.getClub(club.getId()).block();
        assertThat(result).isNotNull();
    }

    @Test
    public void delete() {
        Club club = DomainUtil.createClub(clubRepository);
        clubService.delete(club.getId()).block();

        Club result = clubService.getClub(club.getId()).block();
        assertThat(result).isNull();
    }

    @Test
    public void join() {
        Club club = DomainUtil.createClub(clubRepository);
        User user = DomainUtil.createUser(userRepository);
        user.setName("ddd");
        User user2 = DomainUtil.createUser(userRepository);
        user2.setName("eee");
        club.getUsers().add(user);
        club.getUsers().add(user2);
        user.setClub(club);
        user2.setClub(club);
        userRepository.save(user).then(clubRepository.save(club)).block();
        userRepository.save(user2).then(clubRepository.save(club)).block();

        Mono<User> u = userRepository.findById(user.getId());
        Mono<Club> c = clubRepository.findById(club.getId());
        Mono.zip(c, u).flatMap( tuple-> {
            Club x = tuple.getT1();
            User y = tuple.getT2();
            y.setClub(null);
            x.getUsers().remove(y);
            return Mono.zip(clubRepository.save(x), userRepository.save(y)).then();
        }).block();
        // TODO
        Club result = clubService.getClub(club.getId()).block();

    }

//    public Mono<Club> addUser(User user, ClubRepository clubRepository, UserRepository userRepository) {
//        user.setClub(this);
//        users.add(user);
//        return userRepository.save(user).then(clubRepository.save(this));
//    }
//
//    public Mono<Club> removeUser(User user, ClubRepository clubRepository, UserRepository userRepository) {
////        user.setClub(null);
//        users.remove(user);
//        return userRepository.save(user).then(clubRepository.save(this));
//    }

//    @Test
//    public void leave() {
//        Club club = DomainUtil.createClub(clubRepository);
//        User user1 = DomainUtil.createUser(userRepository);
//        User user2 = DomainUtil.createUser(userRepository);
//
//        clubService.join(club.getId(), user1.getId()).block();
//        clubService.join(club.getId(), user2.getId()).block();
//
//        List<User> results =  userRepository.findByClubId(club.getId()).toStream().collect(toList());
//        System.out.println(results);
//
////        Club result = clubService.getClub(club.getId()).block();
////        System.out.println(result);
//
////        clubService.leave(club.getId(), user2.getId()).block();
////        Club result = clubService.getClub(club.getId()).block();
////
////        assertThat(result.getUsers().size()).isEqualTo(1);
////        assertThat(result.getUsers().get(0).getId()).isEqualTo(user1.getId());
//    }
}
