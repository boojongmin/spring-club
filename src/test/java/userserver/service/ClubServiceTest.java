package userserver.service;

import com.mongodb.BasicDBObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import userserver.domain.Club;
import userserver.domain.User;
import userserver.model.TestModelFactory;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;
import userserver.util.TestUtil;

import java.util.Arrays;
import java.util.List;

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
        this.clubService = new ClubService(clubRepository, pageSize);
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
        TestUtil.createClubs(clubRepository);
        long count = clubService.getList(0)
                .toStream()
                .count();
        assertThat(count).isEqualTo(pageSize);
    }

    @Test
    public void getUser() {
        Club club = TestUtil.createClub(clubRepository);
        Club result = clubService.getClub(club.getId()).block();
        assertThat(result).isNotNull();
    }

    @Test
    public void delete() {
        Club club = TestUtil.createClub(clubRepository);
        clubService.delete(club.getId()).block();

        Club result = clubService.getClub(club.getId()).block();
        assertThat(result).isNull();
    }

    @Test
    public void join() {
        User user1 = TestModelFactory.createUser();
        String name = "user1Name";
        user1.setName(name);
        user1 = userRepository.save(user1).block();
        User user2 = TestUtil.createUser(userRepository);
        User user3 = TestUtil.createUser(userRepository);

        Club club = TestUtil.createClub(clubRepository);
        club.setUsers(Arrays.asList(user1, user2));
        clubRepository.save(club).block();

        List<User> list = club.getUsers().stream().filter(x -> name.equals(x.getName())).collect(toList());
        club.setUsers(list);
        clubRepository.save(club).block();


    }
}
