package com.example.demo.controller;

import com.example.demo.entity.ClassEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.ClassRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private AdminService adminService;

    // GET ALL USERS (with class information)
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            // Add class information to each user
            List<Map<String, Object>> userList = users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getName());
                userMap.put("email", user.getEmail());
                userMap.put("phone", user.getPhone());
                userMap.put("role", user.getRole());
                userMap.put("classId", user.getClassId());
                userMap.put("createdAt", user.getCreatedAt());
                
                // Get class information if classId exists
                if (user.getClassId() != null) {
                    classRepository.findById(user.getClassId()).ifPresent(cls -> {
                        Map<String, Object> classInfo = new HashMap<>();
                        classInfo.put("id", cls.getId());
                        classInfo.put("name", cls.getName());
                        userMap.put("classInfo", classInfo);
                    });
                }
                
                return userMap;
            }).toList();
            
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching users: " + e.getMessage());
        }
    }

    // GET USER BY ID (for editing)
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("phone", user.getPhone());
            response.put("role", user.getRole());
            response.put("classId", user.getClassId());
            response.put("password", user.getPassword()); // Send password for editing
            response.put("createdAt", user.getCreatedAt());
            
            // Add class information
            if (user.getClassId() != null) {
                classRepository.findById(user.getClassId()).ifPresent(cls -> {
                    Map<String, Object> classInfo = new HashMap<>();
                    classInfo.put("id", cls.getId());
                    classInfo.put("name", cls.getName());
                    response.put("classInfo", classInfo);
                });
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching user: " + e.getMessage());
        }
    }

    // UPDATE USER
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Update basic fields
            if (request.containsKey("name")) {
                user.setName((String) request.get("name"));
            }
            if (request.containsKey("email")) {
                user.setEmail((String) request.get("email"));
            }
            if (request.containsKey("phone")) {
                user.setPhone((String) request.get("phone"));
            }
            if (request.containsKey("role")) {
                user.setRole(((String) request.get("role")).toUpperCase());
            }
            
            // Update password if provided
            if (request.containsKey("password") && request.get("password") != null) {
                String password = (String) request.get("password");
                if (!password.isEmpty()) {
                    user.setPassword(password);
                }
            }
            
            // Update class assignment
            if (request.containsKey("batch")) {
                Object batchObj = request.get("batch");
                if (batchObj != null && !batchObj.toString().isEmpty()) {
                    try {
                        user.setClassId(Long.parseLong(batchObj.toString()));
                    } catch (NumberFormatException e) {
                        user.setClassId(null);
                    }
                } else {
                    user.setClassId(null);
                }
            }
            
            User savedUser = userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User updated successfully");
            response.put("user", savedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating user: " + e.getMessage());
        }
    }

    // CREATE STUDENT
    @PostMapping("/create-student")
    public ResponseEntity<?> createStudent(@RequestBody Map<String, Object> request) {
        try {
            User user = new User();
            user.setName((String) request.get("name"));
            user.setEmail((String) request.get("email"));
            user.setPassword((String) request.get("password"));
            user.setRole("STUDENT");
            
            if (request.containsKey("phone")) {
                user.setPhone((String) request.get("phone"));
            }
            
            // Handle batch/class assignment
            if (request.containsKey("batch")) {
                Object batchObj = request.get("batch");
                if (batchObj != null && !batchObj.toString().isEmpty()) {
                    try {
                        user.setClassId(Long.parseLong(batchObj.toString()));
                    } catch (NumberFormatException e) {
                        user.setClassId(null);
                    }
                }
            }

            User saved = adminService.createStudent(user);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating student: " + e.getMessage());
        }
    }

    // CREATE TEACHER
    @PostMapping("/create-teacher")
    public ResponseEntity<?> createTeacher(@RequestBody Map<String, Object> request) {
        try {
            User user = new User();
            user.setName((String) request.get("name"));
            user.setEmail((String) request.get("email"));
            user.setPassword((String) request.get("password"));
            user.setRole("TEACHER");
            
            if (request.containsKey("phone")) {
                user.setPhone((String) request.get("phone"));
            }
            
            // Handle batch/class assignment for teachers
            if (request.containsKey("batch")) {
                Object batchObj = request.get("batch");
                if (batchObj != null && !batchObj.toString().isEmpty()) {
                    try {
                        user.setClassId(Long.parseLong(batchObj.toString()));
                    } catch (NumberFormatException e) {
                        user.setClassId(null);
                    }
                }
            }

            User saved = adminService.createTeacher(user);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating teacher: " + e.getMessage());
        }
    }

    // CREATE ADMIN
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody Map<String, String> request) {
        try {
            User user = new User();
            user.setName(request.get("name"));
            user.setEmail(request.get("email"));
            user.setPassword(request.get("password"));
            user.setRole("ADMIN");
            
            if (request.containsKey("phone")) {
                user.setPhone(request.get("phone"));
            }

            User saved = adminService.createAdmin(user);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating admin: " + e.getMessage());
        }
    }

    // DELETE USER
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error deleting user: " + e.getMessage());
        }
    }
}