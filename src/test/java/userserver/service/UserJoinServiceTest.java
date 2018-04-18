package userserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import userserver.domain.User;
import userserver.model.TestModelFactory;
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
public class UserJoinServiceTest {
    @Autowired UserRepository repository;
    UserService userService;
    @Value("${clubservice.paging-size}") int pageSize;

    @Before
    public void setUp() {
        this.userService = new UserService(repository, pageSize);
        repository.deleteAll().block();
    }

    @Test
    public void createUser() {
        User user = userService.save(TestModelFactory.createUser()).block();
        assertThat(user.getId()).isNotBlank();
    }

    @Test
    public void getUserList() {
        DomainUtil.createUsers(repository);
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
        User user = DomainUtil.createUser(repository);
        User resultUser = userService.getUser(user.getId()).block();
        assertThat(resultUser).isNotNull();
    }

    @Test
    public void deleteUser() {
        User user = DomainUtil.createUser(repository);
        userService.delete(user.getId()).block();

        User resultUser = userService.getUser(user.getId()).block();
        assertThat(resultUser).isNull();
    }
}
