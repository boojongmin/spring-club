package userserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.server.ServerRequest;

@Slf4j
public class ServerRequestUtil {
    public static String getPathVariable(ServerRequest serverRequest, String path) {
        String value = "";
        try {
            value = serverRequest.pathVariable(path);
        } catch (IllegalArgumentException e) {
            log.debug("path variable is null");
        }
        return value;
    }
}
