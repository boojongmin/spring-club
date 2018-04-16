package userserver.model;

import userserver.domain.Club;
import userserver.domain.User;

import java.util.Date;

public class TestModelFactory {
    public static User createUser() {
        return new User("testname", 20, new Date());
    }

    public static Club createClub() {
        return new Club("test name", 19, new Date());
    }
}
