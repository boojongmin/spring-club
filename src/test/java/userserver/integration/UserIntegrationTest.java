package userserver.integration;

import org.springframework.http.HttpStatus;
import userserver.domain.Club;
import userserver.domain.User;
import userserver.model.TestModelFactory;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import userserver.util.DomainUtil;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserIntegrationTest {
    @Autowired WebTestClient client;
    @Autowired UserRepository userRepository;
    @Autowired ClubRepository clubRepository;
    @LocalServerPort private int port;

    @Before
    public void setup() {
        userRepository.deleteAll().block();
        clubRepository.deleteAll().block();

        this.client = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofMillis(Long.MAX_VALUE))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }


    @Test
    public void save_create() {
        User user = TestModelFactory.createUser();
        this.client
                .post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(user))
                .exchange()
                .expectStatus().isCreated();
        StepVerifier.create(userRepository.findAll())
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    public void save_create_user_invalid_request_body_then_return_status_code_422() {
        User user = TestModelFactory.createUser();
        // set invalid model
        user.setName("");
        user.setAge(-1);
        this.client
                .post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(user))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
//                .is4xxClientError()
    }


    @Test
    public void save_update() {
        User user = DomainUtil.createUser(userRepository);
        user.setName("modify name");
        user.setAge(999);
        this.client
                .put()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(user))
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(userRepository.findAll())
                .consumeNextWith(x -> {
                    assertThat(x.getId()).isEqualTo(user.getId());
                    assertThat(x.getName()).isEqualTo(user.getName());
                    assertThat(x.getAge()).isEqualTo(user.getAge());
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void get() {
        User user = DomainUtil.createUser(userRepository);
        this.client
                .get()
                .uri(String.format("/api/user/%s", user.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .consumeWith(x -> {
                    User result = x.getResponseBody();
                    assertThat(result.getId()).isEqualTo(user.getId());
                    assertThat(result.getName()).isEqualTo(user.getName());
                    assertThat(result.getAge()).isEqualTo(user.getAge());
                });
    }

    @Test
    public void getList() {
        DomainUtil.createUsers(userRepository);
        this.client
                .get()
                .uri("/api/user/list/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .consumeWith(x -> {
                    List list = x.getResponseBody();
                    assertThat(list.size()).isEqualTo(10);
                });

        this.client
                .get()
                .uri("/api/user/list")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .consumeWith(x -> {
                    List list = x.getResponseBody();
                    assertThat(list.size()).isEqualTo(10);
                });

        this.client
                .get()
                .uri("/api/user/list/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .consumeWith(x -> {
                    List list = x.getResponseBody();
                    assertThat(list.size()).isEqualTo(10);
                });
    }

    @Test
    public void delete() {
        User user = DomainUtil.createUser(userRepository);
        this.client
                .delete()
                .uri(String.format("/api/user/%s", user.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class)
                .consumeWith(x -> {
                    assertThat(x.getStatus()).isEqualTo(HttpStatus.OK);
                });
    }


    @Test
    public void getClub() {
        User user = DomainUtil.createUser(userRepository);
        Club club = DomainUtil.createClub(clubRepository);
        user.setClubId(club.getId());
        userRepository.save(user).block();

        this.client
                .get()
                .uri(String.format("/api/user/club/%s", user.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Club.class)
                .consumeWith(x -> {
                    Club result = x.getResponseBody();
                    assertThat(result.getId()).isEqualTo(club.getId());
                    assertThat(result.getName()).isEqualTo(club.getName());
                    assertThat(result.getMinAgeForJoin()).isEqualTo(club.getMinAgeForJoin());
                });
    }

}