package userserver.integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import userserver.domain.Club;
import userserver.domain.User;
import userserver.handler.model.ClubUserId;
import userserver.handler.model.UserId;
import userserver.model.TestModelFactory;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;
import userserver.util.DomainUtil;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClubIntegrationTest {
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


    /**
     *    INSERT, UPDATE
     */
    @Test
    public void save_create() {
        Club club = TestModelFactory.createClub();
        this.client
                .post()
                .uri("/api/club")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(club))
                .exchange()
                .expectStatus()
                .isCreated();

        StepVerifier.create(clubRepository.findAll())
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    public void save_update() {
        Club club = DomainUtil.createClub(clubRepository);
        club.setName("modify name");
        club.setMinAgeForJoin(999);
        this.client
                .put()
                .uri("/api/club")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(club))
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(clubRepository.findAll())
                .consumeNextWith(x -> {
                    assertThat(x.getId()).isEqualTo(club.getId());
                    assertThat(x.getName()).isEqualTo(club.getName());
                    assertThat(x.getMinAgeForJoin()).isEqualTo(club.getMinAgeForJoin());
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void createUser_invalid_request_body_then_return_status_code_422() {
        Club club = TestModelFactory.createClub();
        // set invalid model
        club.setName("");
        club.setMinAgeForJoin(-1);
        this.client
                .post()
                .uri("/api/club")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(club))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     *  SELECT
     */

    @Test
    public void get() {
        Club club = DomainUtil.createClub(clubRepository);

        this.client
                .get()
                .uri(String.format("/api/club/%s", club.getId()))
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


    @Test
    public void getUserList() {
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

    /**
     * DELETE
     */
    @Test
    public void getDelete() {
        Club club = DomainUtil.createClub(clubRepository);
        this.client
                .delete()
                .uri(String.format("/api/club/%s", club.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class)
                .consumeWith(x -> {
                    assertThat(x.getStatus()).isEqualTo(HttpStatus.OK);
                });
    }

    /**
     * PROCEDURE
     */

    @Test
    public void join() {
        Club club = DomainUtil.createClub(clubRepository);
        User user = DomainUtil.createUser(userRepository);
        Map<String, String> map = new HashMap<>();
        map.put("clubId", club.getId());
        map.put("userId", user.getId());
        this.client
                .post()
                .uri("/api/club/join")
                .body(fromObject(map))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class)
                .consumeWith(x -> {
                    assertThat(x.getStatus()).isEqualTo(HttpStatus.OK);
                });
    }

    @Test
    public void join_not_found() {
        Map<String, String> map = new HashMap<>();
        map.put("clubId", "");
        map.put("userId", "");
        this.client
                .post()
                .uri(String.format("/api/club/join"))
                .body(fromObject(map))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class)
                .consumeWith(x -> {
                    assertThat(x.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    public void leave() {
        User user = DomainUtil.createUser(userRepository);
        UserId m = new UserId(user.getId());
        this.client
                .post()
                .uri("/api/club/leave")
                .body(fromObject(m))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class)
                .consumeWith(x -> {
                    assertThat(x.getStatus()).isEqualTo(HttpStatus.OK);
                });
    }

    @Test
    public void leave_not_found_then_404() {
        UserId m = new UserId("unkown user id");
        this.client
                .put()
                .uri("/api/club/leave")
                .body(fromObject(m))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class)
                .consumeWith(x -> {
                    assertThat(x.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }


}