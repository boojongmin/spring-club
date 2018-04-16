package userserver.handler;

import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
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


    public Mono<ServerResponse> getUserList(ServerRequest serverRequest) {
        String pageStr = getPathVariable(serverRequest, "page");
        int page = StringUtils.isEmpty(pageStr) ? 0 : Integer.parseInt(pageStr);
        Flux<User> body = userService.getList(page);
//                 TODO check blocking?
//                .collect(Collectors.toList());
        return ok().body(fromPublisher(body, User.class));
    }

    public Mono<ServerResponse> getUser(ServerRequest serverRequest) {
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        String id = getPathVariable(serverRequest, "id");
        return userService.getUser(id)
                .flatMap(x -> ok().body(fromObject(x)) )
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> saveUser(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(User.class)
                .flatMap(validator::validate)
                .flatMap(userService::save)
                .then( serverRequest.method() == HttpMethod.POST ?
                            created(URI.create("/api/user/select")).build() : ok().build() );
    }

    public Mono<ServerResponse> deleteUser(ServerRequest serverRequest) {
        String id = getPathVariable(serverRequest, "id");
        return userService.delete(id)
                .then(ok().build());
    }
}
