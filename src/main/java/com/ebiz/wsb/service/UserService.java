package com.ebiz.wsb.service;

import com.ebiz.wsb.model.User;
import com.ebiz.wsb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user){
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username){
        return userRepository.findByUsername(username);
    }
}
