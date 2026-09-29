package io.github.bayramsevim.reservation.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {

    @Test
    void blankEmailIsRejected(){
        assertThrows(IllegalArgumentException.class,()-> new User(" ","123456"));
    }

    @Test
    void newUserHasUserRole(){
        User user = new User("ali@example.com", "123456");
        assertEquals(Role.USER, user.getRole());
    }
}
