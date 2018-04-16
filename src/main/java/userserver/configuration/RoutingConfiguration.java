package userserver.configuration;

import lombok.extern.slf4j.Slf4j;
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
        // TODO
        // TODO GET은 body에 값을넣을 수 없기 때문에 Content-Type application-json이 의미가없는데... rest api 설계시...
        final RequestPredicate LIST_PREDICATE = GET("/api/user/list/{page}").or(GET("/api/user/list"));
        final RequestPredicate SAVE_PREDICATE = POST("/api/user").or(PUT("/api/user")).and(JSON_PREDICATE );
        final RequestPredicate USER_PREDICATE = GET("/api/user/{id}");
        final RequestPredicate DELETE_PREDICATE = DELETE("/api/user/{id}");

        return route(LIST_PREDICATE,userHandler::getUserList)
                .andRoute(SAVE_PREDICATE, userHandler::saveUser)
                .andRoute(USER_PREDICATE, userHandler::getUser)
                .andRoute(DELETE_PREDICATE, userHandler::deleteUser)
                .filter((request, next) -> {
                    log.debug("do something!!");
                    return next.handle(request);
                });
    }

//    @Bean
//    public RouterFunction<ServerResponse> clubRouter(UserHandler userHandler) {
//        return route(POST("/api/user/select").and(accept), userHandler::getUser)
//                .andRoute(POST("/api/user/create").and(accept), userHandler::save)
//                .filter((request, next) -> {
//                    log.debug("do something!!");
//                    return next.handle(request);
//                });
//    }
}
