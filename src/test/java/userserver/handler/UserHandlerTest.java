package userserver.handler;

import org.springframework.http.HttpMethod;
import reactor.core.publisher.Flux;
import userserver.domain.User;
import userserver.handler.validator.ModelValidator;
import userserver.model.TestModelFactory;
import userserver.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@RunWith(SpringRunner.class)
public class UserHandlerTest {
    UserHandler handler;
    @MockBean UserService userService;
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    ModelValidator modelValidator = new ModelValidator(validator);

    @Before
    public void setup() {
        this.handler = new UserHandler(userService, modelValidator);
    }

    @Test
    public void saveUser() {
        User user = TestModelFactory.createUser();
        MockServerRequest postRequest = MockServerRequest.builder().method(HttpMethod.POST).body(Mono.just(user));
        MockServerRequest putRequest = MockServerRequest.builder().method(HttpMethod.PUT).body(Mono.just(user));
        given(userService.save(user)).willReturn(Mono.empty());

        Mono<ServerResponse> postResponse = handler.saveUser(postRequest);
        Mono<ServerResponse> putResponse = handler.saveUser(putRequest);

        StepVerifier.create(postResponse)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.CREATED);
                })
                .expectComplete()
                .verify();

        StepVerifier.create(putResponse)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .expectComplete()
                .verify();

    }

    @Test
    public void getUserList() {
        MockServerRequest request = MockServerRequest.builder().pathVariable("page", "0").build();
        given(userService.getList(0)).willReturn(Flux.empty());
        Mono<ServerResponse> response = handler.getUserList(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void getUser() {
        User user = TestModelFactory.createUser();
        user.setId("testid");
        MockServerRequest request = MockServerRequest.builder().pathVariable("id", user.getId()).build();
        given(userService.getUser(user.getId())).willReturn(Mono.just(user));
        Mono<ServerResponse> response = handler.getUser(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.OK);
                    // TODO BodyInserterServerResponse response body의 값을 가져올 수가 없다... 확인 필요
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void deleteUser() {
        MockServerRequest request = MockServerRequest.builder().body(Mono.just("testId"));
        given(userService.delete(any())).willReturn(Mono.empty());
        Mono<ServerResponse> response = handler.deleteUser(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .expectComplete()
                .verify();
    }



}