package userserver.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import userserver.domain.Club;
import userserver.domain.User;
import userserver.enums.JoinResult;
import userserver.handler.model.ClubUserId;
import userserver.handler.model.UserId;
import userserver.handler.validator.ModelValidator;
import userserver.model.TestModelFactory;
import userserver.service.ClubService;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@RunWith(SpringRunner.class)
public class ClubHandlerTest {
    ClubHandler handler;
    @MockBean ClubService clubService;
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    ModelValidator modelValidator = new ModelValidator(validator);

    @Before
    public void setup() {
        this.handler = new ClubHandler(clubService, modelValidator);
    }

    @Test
    public void saveClub() {
        Club club = TestModelFactory.createClub();
        MockServerRequest postRequest = MockServerRequest.builder().method(HttpMethod.POST).body(Mono.just(club));
        given(clubService.save(club)).willReturn(Mono.empty());

        Mono<ServerResponse> postResponse = handler.save(postRequest);

        StepVerifier.create(postResponse)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(x.headers().get("Location").get(0)).isEqualTo("/api/club");
                })
                .expectComplete()
                .verify();

    }

    @Test
    public void get() {
        MockServerRequest request = MockServerRequest.builder().pathVariable("id", "testId").build();
        Mono<Club> mockResult = Mono.just(new Club("name", 10, new Date()));
        given(clubService.get("testId")).willReturn(mockResult);
        Mono<ServerResponse> response = handler.get(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void getUserList() {
        MockServerRequest request = MockServerRequest.builder().pathVariable("page", "0").build();
        given(clubService.getList(0)).willReturn(Flux.empty());
        Mono<ServerResponse> response = handler.getList(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void delete() {
        MockServerRequest request = MockServerRequest.builder().body(Mono.just("testId"));
        given(clubService.delete(any())).willReturn(Mono.empty());
        Mono<ServerResponse> response = handler.delete(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .expectComplete()
                .verify();
    }



    @Test
    public void join() {
        Map<String, String> map = new HashMap<>();
        MockServerRequest request = MockServerRequest.builder().body(Mono.just(map));
        given(clubService.join(any(), any())).willReturn(Mono.just(JoinResult.SUCCESS));
        Mono<ServerResponse> response = handler.join(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void join_not_allow_age() {
        Map<String, String> map = new HashMap<>();
        MockServerRequest request = MockServerRequest.builder().body(Mono.just(map));
        given(clubService.join(any(), any())).willReturn(Mono.just(JoinResult.FAIL_NOT_ALLOW_AGE));
        Mono<ServerResponse> response = handler.join(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void join_not_found() {
        Map<String, String> map = new HashMap<>();
        MockServerRequest request = MockServerRequest.builder().body(Mono.just(map));
        given(clubService.join(any(), any())).willReturn(Mono.just(JoinResult.NOT_FOUND));
        Mono<ServerResponse> response = handler.join(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void leave() {
        UserId m = new UserId("user");
        MockServerRequest request = MockServerRequest.builder().body(Mono.just(m));
        given(clubService.leave(any())).willReturn(Mono.just(new User()));
        Mono<ServerResponse> response = handler.leave(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void leave_user_not_found_then_404() {
        MockServerRequest request = MockServerRequest.builder().body(Mono.empty());
        given(clubService.leave(any())).willReturn(Mono.just(new User()));
        Mono<ServerResponse> response = handler.leave(request);
        StepVerifier.create(response)
                .consumeNextWith(x -> {
                    assertThat(x.statusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                })
                .expectComplete()
                .verify();
    }
}