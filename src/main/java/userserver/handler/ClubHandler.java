package userserver.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import userserver.domain.Club;
import userserver.enums.JoinResult;
import userserver.handler.model.ClubUserId;
import userserver.handler.model.UserId;
import userserver.handler.validator.ModelValidator;
import userserver.service.ClubService;

import java.net.URI;
import java.util.Map;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static userserver.util.ServerRequestUtil.getPathVariable;

@Component
@RequiredArgsConstructor
public class ClubHandler {
    @NonNull private final ClubService clubService;
    @NonNull private final ModelValidator validator;

    public Mono<ServerResponse> save(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(Club.class)
                .flatMap(validator::validate)
                .flatMap(clubService::save)
                .then( serverRequest.method() == HttpMethod.POST ?
                            created(URI.create("/api/club")).build() : ok().build() );
    }

    public Mono<ServerResponse> getList(ServerRequest serverRequest) {
        String pageStr = getPathVariable(serverRequest, "page");
        int page = StringUtils.isEmpty(pageStr) ? 0 : Integer.parseInt(pageStr);
        Flux<Club> body = clubService.getList(page);
        return ok().body(fromPublisher(body, Club.class));
    }

    public Mono<ServerResponse> get(ServerRequest serverRequest) {
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        String id = getPathVariable(serverRequest, "id");
        return clubService.get(id)
                .flatMap(x -> ok().body(fromObject(x)) )
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        String id = getPathVariable(serverRequest, "id");
        return clubService.delete(id)
                .then(ok().build());
    }

    public Mono<ServerResponse> join(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Map.class)
                .flatMap(this::findUserAndClub);
    }

    private Mono<ServerResponse> findUserAndClub(Map<String, String> map) {
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        String clubId = map.get("clubId");
        String userId = map.get("userId");
        return clubService.join(clubId, userId)
                .flatMap(x -> {
                    if(x == JoinResult.NOT_FOUND) {
                        return notFound;
                    } else if(x == JoinResult.FAIL_NOT_ALLOW_AGE || x == JoinResult.FAIL_CLUB_IS_JOINED) {
                        return ServerResponse.status(HttpStatus.NOT_ACCEPTABLE).build();
                    }
                    return ok().build();
                });
    }

    public Mono<ServerResponse> leave(ServerRequest serverRequest) {
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return serverRequest.bodyToMono(UserId.class)
                .flatMap(this::findUser)
                .switchIfEmpty(notFound);
    }

    private Mono<ServerResponse> findUser(UserId m) {
        return clubService.leave(m.getUserId())
                .flatMap(x -> ok().build());
    }
}
