package com.example.demo.controller;

import com.example.demo.entity.Application;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    // Submit new application
    @PostMapping("/submit")
    public ResponseEntity<?> submitApplication(@RequestBody Map<String, String> request) {
        try {
            Application application = new Application();
            application.setName(request.get("name"));
            application.setEmail(request.get("email"));
            application.setPhone(request.get("phone"));
            application.setYear(request.get("year"));
            application.setCourse(request.get("course"));
            application.setCollege(request.get("college"));
            application.setStatus("PENDING");
            
            Application saved = applicationRepository.save(application);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error submitting application: " + e.getMessage());
        }
    }

    // Get all applications
    @GetMapping("/all")
    public ResponseEntity<?> getAllApplications() {
        try {
            List<Application> applications = applicationRepository.findAll();
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching applications: " + e.getMessage());
        }
    }

    // Approve application
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveApplication(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Application application = applicationRepository.findById(id).orElse(null);
            if (application == null) {
                return ResponseEntity.notFound().build();
            }

            // Create user account
            User user = new User();
            user.setName(application.getName());
            user.setEmail(application.getEmail());
            user.setPhone(application.getPhone());
            user.setPassword("student123"); // Default password
            user.setRole("STUDENT");
            
            // Assign to class if provided
            if (request.containsKey("classId")) {
                Object classIdObj = request.get("classId");
                if (classIdObj != null) {
                    user.setClassId(Long.parseLong(classIdObj.toString()));
                }
            }
            
            userRepository.save(user);
            
            // Update application status
            application.setStatus("APPROVED");
            applicationRepository.save(application);
            
            return ResponseEntity.ok(Map.of("message", "Application approved successfully", "user", user));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error approving application: " + e.getMessage());
        }
    }

    // Reject application
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectApplication(@PathVariable Long id) {
        try {
            Application application = applicationRepository.findById(id).orElse(null);
            if (application == null) {
                return ResponseEntity.notFound().build();
            }

            application.setStatus("REJECTED");
            applicationRepository.save(application);
            
            return ResponseEntity.ok(Map.of("message", "Application rejected"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error rejecting application: " + e.getMessage());
        }
    }

    // Delete application
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long id) {
        try {
            applicationRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Application deleted"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error deleting application: " + e.getMessage());
        }
    }
}