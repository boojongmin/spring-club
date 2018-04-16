package userserver.integration;

import org.springframework.http.HttpStatus;
import userserver.domain.User;
import userserver.model.TestModelFactory;
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
import userserver.util.TestUtil;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserIntegrationTest {
    @Autowired WebTestClient client;
    @Autowired UserRepository userRepository;
    @LocalServerPort private int port;

    @Before
    public void setup() {
        userRepository.deleteAll().block();

        this.client = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofMillis(Long.MAX_VALUE))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @Test
    public void getUser() {
        User user = TestUtil.createUser(userRepository);

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
    public void createUser() {
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
    public void updateUser() {
        User user = TestUtil.createUser(userRepository);
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
    public void getUserList() {
        TestUtil.createUsers(userRepository);
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
    public void getDeleteUser() {
        User user = TestUtil.createUser(userRepository);
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
    public void createUser_invalid_request_body_then_return_status_code_422() {
        User user = TestModelFactory.createUser();
        // set invalid model
        user.setName("");
        user.setAge(-1);
        this.client
                .post()
                .uri("/api/user/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(user))
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }
}