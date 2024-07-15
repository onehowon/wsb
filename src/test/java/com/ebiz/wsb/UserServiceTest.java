package com.ebiz.wsb;

import com.ebiz.wsb.model.User;
import com.ebiz.wsb.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    public void testRegisterUser(){
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        User savedUser = userService.saveuser(user);
        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
    }
}
