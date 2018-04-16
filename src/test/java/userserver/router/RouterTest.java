package userserver.router;

import userserver.configuration.RoutingConfiguration;
import userserver.domain.User;
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
    @Autowired RouterFunction router;
    @MockBean UserHandler handler;
    private WebTestClient testClient;

    @Before
    public void setUp() {
        this.testClient = WebTestClient.bindToRouterFunction(router).build();
    }

    @Test
    public void createUser() {
        User user = TestModelFactory.createUser();
        given(handler.saveUser(any())).willReturn(created(any()).build());
        this.testClient
                .post()
                .uri("/api/user")
                .body(fromObject(user))
                .exchange()
                .expectStatus().isCreated();

        this.testClient
                .put()
                .uri("/api/user")
                .body(fromObject(user))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    public void getUserList() {
        given(handler.getUserList(any())).willReturn(ok().build());
        this.testClient
                .get()
                .uri("/api/user/list")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void getUser() {
        given(handler.getUser(any())).willReturn(ok().build());
        this.testClient
                .get()
                .uri("/api/user/select")
                .exchange()
                .expectStatus().isOk();
    }
}

