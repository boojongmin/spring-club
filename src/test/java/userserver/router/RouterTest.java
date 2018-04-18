package userserver.router;

import org.springframework.beans.factory.annotation.Qualifier;
import userserver.configuration.RoutingConfiguration;
import userserver.domain.User;
import userserver.handler.ClubHandler;
import userserver.handler.UserHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import userserver.model.TestModelFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@RunWith(SpringRunner.class)
@Import(RoutingConfiguration.class)
public class RouterTest {
    @Autowired @Qualifier("userRouter") RouterFunction userRouter;
    @Autowired @Qualifier("clubRouter") RouterFunction clubRouter;
    @MockBean UserHandler userHandler;
    @MockBean ClubHandler clubHandler;
    private WebTestClient testClient;

    @Before
    public void setUp() {
        this.testClient = WebTestClient.bindToRouterFunction(userRouter.and(clubRouter)).build();
    }

    /**
     *  User Router Test
     */
    @Test
    public void createUser() {
        User user = TestModelFactory.createUser();
        given(userHandler.save(any())).willReturn(created(any()).build());
        this.testClient.post().uri("/api/user").body(fromObject(user)).exchange().expectStatus().isCreated();

        this.testClient.put().uri("/api/user").body(fromObject(user)).exchange().expectStatus().isCreated();
    }

    @Test
    public void getUserList() {
        given(userHandler.getList(any())).willReturn(ok().build());
        this.testClient.get().uri("/api/user/list").exchange().expectStatus().isOk();
    }

    @Test
    public void getUser() {
        given(userHandler.get(any())).willReturn(ok().build());
        this.testClient.get().uri("/api/user" + "/userid").exchange().expectStatus().isOk();
    }


    /**
     *  Club Router Test
     */
    @Test
    public void createClub() {
        User user = TestModelFactory.createUser();
        given(clubHandler.save(any())).willReturn(created(any()).build());
        this.testClient.post().uri("/api/club").body(fromObject(user)).exchange().expectStatus().isCreated();
        this.testClient.put().uri("/api/club").body(fromObject(user)).exchange().expectStatus().isCreated();
    }

    @Test
    public void getClubList() {
        given(clubHandler.getList(any())).willReturn(ok().build());
        this.testClient.get().uri("/api/club/list").exchange().expectStatus().isOk();
        this.testClient.get().uri("/api/club/list/0").exchange().expectStatus().isOk();
        this.testClient.get().uri("/api/club/list/").exchange().expectStatus().isOk();
    }

    @Test
    public void getClub() {
        given(clubHandler.get(any())).willReturn(ok().build());
        this.testClient.get().uri("/api/club" + "/clubid").exchange().expectStatus().isOk();
    }

    @Test
    public void deleteClub() {
        given(clubHandler.delete(any())).willReturn(ok().build());
        this.testClient.delete().uri("/api/club" + "/clubid").exchange().expectStatus().isOk();
    }

    @Test
    public void joinClub() {
        given(clubHandler.join(any())).willReturn(ok().build());
        this.testClient.post().uri("/api/club/join").exchange().expectStatus().isOk();
    }

    @Test
    public void leaveClub() {
        given(clubHandler.leave(any())).willReturn(ok().build());
        this.testClient.post().uri("/api/club/leave").exchange().expectStatus().isOk();
    }

}

