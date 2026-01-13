package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    public User createAdmin(User user) {
        user.setRole("ADMIN");
        return userRepository.save(user);
    }

    public User createTeacher(User user) {
        user.setRole("TEACHER");
        return userRepository.save(user);
    }

    public User createStudent(User user) {
        user.setRole("STUDENT");
        return userRepository.save(user);
    }
}
