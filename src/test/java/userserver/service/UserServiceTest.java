package userserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import userserver.domain.Club;
import userserver.domain.User;
import userserver.model.TestModelFactory;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import userserver.util.DomainUtil;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
@TestPropertySource("classpath:application.properties")
public class UserServiceTest {
    UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired ClubRepository clubRepository;
    @Value("${clubservice.paging-size}") int pageSize;

    @Before
    public void setUp() {
        this.userService = new UserService(userRepository, clubRepository, pageSize);
        userRepository.deleteAll().block();
    }

    @Test
    public void createUser() {
        User user = userService.save(TestModelFactory.createUser()).block();
        assertThat(user.getId()).isNotBlank();
    }

    @Test
    public void getUserList() {
        DomainUtil.createUsers(userRepository);
        long count = userService.getList(0)
                .toStream()
                .count();
        int sumOfAge = userService.getList(1).toStream().mapToInt(User::getAge).sum();
        int respectSumOfAge = IntStream.range(10, 20).sum();
        assertThat(count).isEqualTo(pageSize);
        assertThat(sumOfAge).isEqualTo(respectSumOfAge);
    }

    @Test
    public void getUser() {
        User user = DomainUtil.createUser(userRepository);
        User resultUser = userService.get(user.getId()).block();
        assertThat(resultUser).isNotNull();
    }

    @Test
    public void deleteUser() {
        User user = DomainUtil.createUser(userRepository);
        userService.delete(user.getId()).block();

        User resultUser = userService.get(user.getId()).block();
        assertThat(resultUser).isNull();
    }

    @Test
    public void getClub() {
        User user = DomainUtil.createUser(userRepository);
        Club club = DomainUtil.createClub(clubRepository);
        user.setClubId(club.getId());
        userRepository.save(user).block();

        Club result = userService.getClub(user.getId()).block();
        assertThat(result.getId()).isEqualTo(club.getId());
        assertThat(result.getName()).isEqualTo(club.getName());
    }
}
