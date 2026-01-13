package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // Login method
    public User login(String email, String password) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Simple password check (plain text for now)
                if (user.getPassword().equals(password)) {
                    // Don't send password back to frontend
                    user.setPassword(null);
                    return user;
                }
            }

            return null; // login failed
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Login error: " + e.getMessage());
        }
    }

    // Create user (Admin / Teacher / Student)
    public User createUser(User user) {
        try {
            // Check if email already exists
            Optional<User> existing = userRepository.findByEmail(user.getEmail());
            if (existing.isPresent()) {
                throw new RuntimeException("Email already exists");
            }
            
            return userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }
}