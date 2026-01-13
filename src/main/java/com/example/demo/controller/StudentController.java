package com.example.demo.controller;

import com.example.demo.entity.ClassEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.ClassRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    // Get student's assigned class
    @GetMapping("/my-class/{studentId}")
    public ResponseEntity<?> getMyClass(@PathVariable Long studentId) {
        try {
            User student = userRepository.findById(studentId).orElse(null);
            
            if (student == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (student.getClassId() == null) {
                return ResponseEntity.ok(Map.of("message", "No class assigned"));
            }
            
            ClassEntity classEntity = classRepository.findById(student.getClassId()).orElse(null);
            
            if (classEntity == null) {
                return ResponseEntity.ok(Map.of("message", "Class not found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", classEntity.getId());
            response.put("name", classEntity.getName());
            response.put("startDate", classEntity.getStartDate());
            response.put("schedule", classEntity.getSchedule());
            response.put("status", classEntity.getStatus());
            response.put("fee", classEntity.getFee());
            
            // Add teacher information
            if (classEntity.getTeacherId() != null) {
                userRepository.findById(classEntity.getTeacherId()).ifPresent(teacher -> {
                    Map<String, Object> teacherInfo = new HashMap<>();
                    teacherInfo.put("id", teacher.getId());
                    teacherInfo.put("name", teacher.getName());
                    teacherInfo.put("email", teacher.getEmail());
                    response.put("teacher", teacherInfo);
                });
            } else if (classEntity.getTeacherName() != null) {
                Map<String, Object> teacherInfo = new HashMap<>();
                teacherInfo.put("name", classEntity.getTeacherName());
                response.put("teacher", teacherInfo);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching class: " + e.getMessage());
        }
    }

    // Get student profile with class info
    @GetMapping("/profile/{studentId}")
    public ResponseEntity<?> getProfile(@PathVariable Long studentId) {
        try {
            User student = userRepository.findById(studentId).orElse(null);
            
            if (student == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", student.getId());
            profile.put("name", student.getName());
            profile.put("email", student.getEmail());
            profile.put("phone", student.getPhone());
            profile.put("role", student.getRole());
            profile.put("classId", student.getClassId());
            
            // Add class information if assigned
            if (student.getClassId() != null) {
                classRepository.findById(student.getClassId()).ifPresent(cls -> {
                    Map<String, Object> classInfo = new HashMap<>();
                    classInfo.put("id", cls.getId());
                    classInfo.put("name", cls.getName());
                    classInfo.put("schedule", cls.getSchedule());
                    classInfo.put("startDate", cls.getStartDate());
                    profile.put("class", classInfo);
                });
            }
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching profile: " + e.getMessage());
        }
    }
}