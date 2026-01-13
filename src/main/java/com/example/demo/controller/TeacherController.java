package com.example.demo.controller;

import com.example.demo.entity.ClassEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.ClassRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "*")
public class TeacherController {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all classes where the teacher is assigned (as teacher)
    @GetMapping("/my-classes/{teacherId}")
    public ResponseEntity<?> getMyClasses(@PathVariable Long teacherId) {
        try {
            // Get classes where this teacher is the teacher
            List<ClassEntity> teacherClasses = classRepository.findByTeacherId(teacherId);
            
            // Also get any class where teacher is assigned via classId
            User teacher = userRepository.findById(teacherId).orElse(null);
            if (teacher != null && teacher.getClassId() != null) {
                ClassEntity assignedClass = classRepository.findById(teacher.getClassId()).orElse(null);
                if (assignedClass != null && !teacherClasses.contains(assignedClass)) {
                    teacherClasses.add(assignedClass);
                }
            }
            
            // Add student count to each class
            List<Map<String, Object>> classesWithStudents = teacherClasses.stream().map(cls -> {
                Map<String, Object> classMap = new HashMap<>();
                classMap.put("id", cls.getId());
                classMap.put("name", cls.getName());
                classMap.put("startDate", cls.getStartDate());
                classMap.put("schedule", cls.getSchedule());
                classMap.put("status", cls.getStatus());
                classMap.put("fee", cls.getFee());
                
                // Count students in this class
                long studentCount = userRepository.findAll().stream()
                    .filter(u -> "STUDENT".equalsIgnoreCase(u.getRole()) && 
                                cls.getId().equals(u.getClassId()))
                    .count();
                classMap.put("studentCount", studentCount);
                
                return classMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(classesWithStudents);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching classes: " + e.getMessage());
        }
    }

    // Get students in teacher's classes
    @GetMapping("/my-students/{teacherId}")
    public ResponseEntity<?> getMyStudents(@PathVariable Long teacherId) {
        try {
            // Get all classes for this teacher
            List<ClassEntity> teacherClasses = classRepository.findByTeacherId(teacherId);
            
            // Get teacher's assigned class as well
            User teacher = userRepository.findById(teacherId).orElse(null);
            if (teacher != null && teacher.getClassId() != null) {
                ClassEntity assignedClass = classRepository.findById(teacher.getClassId()).orElse(null);
                if (assignedClass != null && !teacherClasses.contains(assignedClass)) {
                    teacherClasses.add(assignedClass);
                }
            }
            
            List<Long> classIds = teacherClasses.stream()
                .map(ClassEntity::getId)
                .collect(Collectors.toList());
            
            // Get all students enrolled in these classes
            List<Map<String, Object>> students = userRepository.findAll().stream()
                .filter(u -> "STUDENT".equalsIgnoreCase(u.getRole()) && 
                            u.getClassId() != null && 
                            classIds.contains(u.getClassId()))
                .map(student -> {
                    Map<String, Object> studentMap = new HashMap<>();
                    studentMap.put("id", student.getId());
                    studentMap.put("name", student.getName());
                    studentMap.put("email", student.getEmail());
                    studentMap.put("phone", student.getPhone());
                    studentMap.put("classId", student.getClassId());
                    
                    // Add class name
                    classRepository.findById(student.getClassId()).ifPresent(cls -> {
                        studentMap.put("className", cls.getName());
                    });
                    
                    return studentMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching students: " + e.getMessage());
        }
    }
}