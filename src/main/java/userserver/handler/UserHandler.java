package userserver.handler;

import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import userserver.domain.Club;
import userserver.handler.validator.ModelValidator;
import userserver.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import userserver.domain.User;

import java.net.URI;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static userserver.util.ServerRequestUtil.getPathVariable;

@Component
@RequiredArgsConstructor
public class UserHandler {
    @NonNull private final UserService userService;
    @NonNull private final ModelValidator validator;


    public Mono<ServerResponse> getList(ServerRequest serverRequest) {
        String pageStr = getPathVariable(serverRequest, "page");
        int page = StringUtils.isEmpty(pageStr) ? 0 : Integer.parseInt(pageStr);
        Flux<User> body = userService.getList(page);
        return ok().body(fromPublisher(body, User.class));
    }

    public Mono<ServerResponse> get(ServerRequest serverRequest) {
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        String id = getPathVariable(serverRequest, "id");
        return userService.get(id)
                .flatMap(x -> ok().body(fromObject(x)) )
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> save(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(User.class)
                .flatMap(validator::validate)
                .flatMap(userService::save)
                .then( serverRequest.method() == HttpMethod.POST ?
                            created(URI.create("/api/user")).build() : ok().build() );
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        String id = getPathVariable(serverRequest, "id");
        return userService.delete(id)
                .then(ok().build());
    }

    public Mono<ServerResponse> getClub(ServerRequest serverRequest) {
        String id = getPathVariable(serverRequest, "id");
        Mono<Club> club = userService.getClub(id);
        return club
                .flatMap(x -> ok().body(fromObject(x)));
    }
}
