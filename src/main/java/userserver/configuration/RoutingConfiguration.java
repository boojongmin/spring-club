package userserver.configuration;

import lombok.extern.slf4j.Slf4j;
import userserver.handler.ClubHandler;
import userserver.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Slf4j
public class RoutingConfiguration {
    RequestPredicate JSON_PREDICATE = accept(MediaType.APPLICATION_JSON);

    @Bean
    public RouterFunction<ServerResponse> userRouter(UserHandler userHandler) {
        final RequestPredicate SAVE_PREDICATE = POST("/api/user").or(PUT("/api/user")).and(JSON_PREDICATE );
        final RequestPredicate GET_PREDICATE = GET("/api/user/{id}");
        final RequestPredicate LIST_PREDICATE = GET("/api/user/list/{page}").or(GET("/api/user/list"));
        final RequestPredicate DELETE_PREDICATE = DELETE("/api/user/{id}");
        final RequestPredicate CLUB_PREDICATE = GET("/api/user/club/{id}");

        return route(SAVE_PREDICATE, userHandler::save)
                .andRoute(LIST_PREDICATE,userHandler::getList)
                .andRoute(GET_PREDICATE, userHandler::get)
                .andRoute(DELETE_PREDICATE, userHandler::delete)
                .andRoute(CLUB_PREDICATE, userHandler::getClub)
                .filter((request, next) -> {
                    log.debug("do something!!");
                    return next.handle(request);
                });
    }

    @Bean
    public RouterFunction<ServerResponse> clubRouter(ClubHandler clubHandler) {
        final RequestPredicate SAVE_PREDICATE = POST("/api/club").or(PUT("/api/club")).and(JSON_PREDICATE );
        final RequestPredicate GET_PREDICATE = GET("/api/club/{id}");
        final RequestPredicate LIST_PREDICATE = GET("/api/club/list/{page}") .or(GET("/api/club/list"));
        final RequestPredicate DELETE_PREDICATE = DELETE("/api/club/{id}");
        final RequestPredicate JOIN_PREDICATE = POST("/api/club/join");
        final RequestPredicate LEAVE_PREDICATE = POST("/api/club/leave");

        return route(SAVE_PREDICATE, clubHandler::save)
                .andRoute(LIST_PREDICATE,clubHandler::getList)
                .andRoute(GET_PREDICATE, clubHandler::get)
                .andRoute(DELETE_PREDICATE, clubHandler::delete)
                .andRoute(JOIN_PREDICATE, clubHandler::join)
                .andRoute(LEAVE_PREDICATE, clubHandler::leave)
                .filter((request, next) -> {
                    log.debug("do something!!");
                    return next.handle(request);
                });


    }
}
