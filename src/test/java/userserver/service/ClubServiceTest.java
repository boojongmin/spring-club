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
import userserver.domain.Club;
import userserver.domain.User;
import userserver.enums.JoinResult;
import userserver.model.TestModelFactory;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;
import userserver.util.DomainUtil;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
@TestPropertySource("classpath:application.properties")
public class ClubServiceTest {
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
        Club club = clubService.save(TestModelFactory.createClub()).block();
        assertThat(club.getId()).isNotBlank();
    }

    @Test
    public void getList() {
        DomainUtil.createClubs(clubRepository);
        long count = clubService.getList(0).toStream().count();
        assertThat(count).isEqualTo(pageSize);
    }

    @Test
    public void getClub() {
        Club club = DomainUtil.createClub(clubRepository);
        Club result = clubService.get(club.getId()).block();
        assertThat(result).isNotNull();
    }

    @Test
    public void delete() {
        Club club = DomainUtil.createClub(clubRepository);
        clubService.delete(club.getId()).block();

        Club result = clubService.get(club.getId()).block();
        assertThat(result).isNull();
    }

    @Test
    public void join(){
        Club club = DomainUtil.createClub(clubRepository);
        User user1 = DomainUtil.createUser(userRepository);
        JoinResult joinResult1 = clubService.join(club.getId(), user1.getId()).block();
        assertThat(joinResult1).isEqualTo(JoinResult.SUCCESS);
    }

    // @DBRef를쓰고2회이상저장할때새로운게저장이안된느문제때문에구조변경하고테스트코드추가
    @Test
    public void join_twice(){
        Club club = DomainUtil.createClub(clubRepository);
        User user1 = DomainUtil.createUser(userRepository);
        User user2 = DomainUtil.createUser(userRepository);
        JoinResult joinResult1 = clubService.join(club.getId(), user1.getId()).block();
        JoinResult joinResult2 = clubService.join(club.getId(), user2.getId()).block();
        Long count = userRepository.findByClubId(club.getId()).toStream().count();
        assertThat(joinResult1).isEqualTo(JoinResult.SUCCESS);
        assertThat(joinResult2).isEqualTo(JoinResult.SUCCESS);
        assertThat(count).isEqualTo(2);

    }

    @Test
    public void join_fail_club_is_joined() {
        Club club = DomainUtil.createClub(clubRepository);
        User user = DomainUtil.createUser(userRepository);
        clubService.join(club.getId(), user.getId()).block();
        JoinResult joinResult = clubService.join(club.getId(), user.getId()).block();
        assertThat(joinResult).isEqualTo(JoinResult.FAIL_CLUB_IS_JOINED);
    }

    @Test
    public void join_fail_not_allow_age() {
        Club club = DomainUtil.createClub(clubRepository);
        User user = DomainUtil.createUser(userRepository);
        club.setMinAgeForJoin(100);
        user.setAge(1);
        JoinResult joinResult = clubService.join(club.getId(), user.getId()).block();
        assertThat(joinResult).isEqualTo(JoinResult.SUCCESS);
    }

    @Test
    public void leave() {
        Club club = DomainUtil.createClub(clubRepository);
        User user1 = DomainUtil.createUser(userRepository);
        User user2 = DomainUtil.createUser(userRepository);

        clubService.join(club.getId(), user1.getId()).block();
        clubService.join(club.getId(), user2.getId()).block();

        List<User> tmpUsers =  userRepository.findByClubId(club.getId()).toStream().collect(toList());
        clubService.leave(tmpUsers.get(0).getId()).block();

        List<User> results =  userRepository.findByClubId(club.getId()).toStream().collect(toList());
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0)).isEqualTo(user2);
    }
}
