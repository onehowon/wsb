package com.ebiz.wsb;

import com.ebiz.wsb.model.User;
import com.ebiz.wsb.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testRegisterUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("testuser@example.com");
        user.setPhoneNumber("1234567890");
        user.setUserType("parent");

        User savedUser = userService.saveUser(user);

        assertNotNull(savedUser, "The saved user should not be null");
        assertEquals("testuser", savedUser.getUsername(), "The username should be 'testuser'");
        assertNotNull(savedUser.getPassword(), "The password should be encoded and not null");
        assertEquals("testuser@example.com", savedUser.getEmail(), "The email should be 'testuser@example.com'");
        assertEquals("1234567890", savedUser.getPhoneNumber(), "The phone number should be '1234567890'");
        assertEquals("parent", savedUser.getUserType(), "The user type should be 'parent'");
    }
}
