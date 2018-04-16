package userserver.util;

import reactor.core.publisher.Flux;
import userserver.domain.Club;
import userserver.domain.User;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class TestUtil {
    public static List<User> createUsers(UserRepository userRepository) {
        long now = new Date().getTime();
        List<User> list = IntStream.range(0, 100)
                .mapToObj(x -> new User("testname-" + x, x, new Date(now + x)))
                .collect(toList());
        return userRepository.saveAll(Flux.fromIterable(list)).toStream().collect(toList());
    }

    public static User createUser(UserRepository userRepository) {
        User test = new User("test", 20, new Date());
        return userRepository.save(test).block();
    }

    public static List<Club> createClubs(ClubRepository clubRepository) {
        long now = new Date().getTime();
        List<Club> list = IntStream.range(0, 100)
                .mapToObj(x -> new Club("testname-" + x, x, new Date(now + x)))
                .collect(toList());
        return clubRepository.saveAll(Flux.fromIterable(list)).toStream().collect(toList());
    }

    public static Club createClub(ClubRepository clubRepository) {
        Club test = new Club("test", 19, new Date());
        return clubRepository.save(test).block();
    }
}
