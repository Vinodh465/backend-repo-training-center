package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Bean
    CommandLineRunner initAdmin() {
        return args -> {

            String adminEmail = "admin@visiontranz.com";

            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                User admin = new User();
                admin.setName("Admin");
                admin.setEmail(adminEmail);
                admin.setPassword("admin123"); // we will encrypt later
                admin.setRole("ADMIN");

                userRepository.save(admin);

                System.out.println("✅ Permanent Admin Created");
            } else {
                System.out.println("ℹ️ Admin already exists");
            }
        };
    }
}
