package com.example.demo.controller;

import com.example.demo.entity.ClassEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private UserRepository userRepository;

    // GET ALL CLASSES
    @GetMapping("/classes")
    public ResponseEntity<?> getAllClasses() {
        try {
            List<ClassEntity> classes = classService.getAllClasses();
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching classes: " + e.getMessage());
        }
    }

    // GET CLASS BY ID
    @GetMapping("/classes/{id}")
    public ResponseEntity<?> getClassById(@PathVariable Long id) {
        try {
            return classService.getClassById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching class: " + e.getMessage());
        }
    }

    // CREATE CLASS
    @PostMapping("/classes")
    public ResponseEntity<?> createClass(@RequestBody Map<String, Object> request) {
        try {
            ClassEntity classEntity = new ClassEntity();
            classEntity.setName((String) request.get("name"));
            classEntity.setStartDate((String) request.get("startDate"));
            classEntity.setSchedule((String) request.get("schedule"));
            classEntity.setStatus((String) request.get("status"));
            
            // Handle fee - could be String or Number
            Object feeObj = request.get("fee");
            if (feeObj instanceof Number) {
                classEntity.setFee(((Number) feeObj).doubleValue());
            } else if (feeObj instanceof String) {
                classEntity.setFee(Double.parseDouble((String) feeObj));
            }

            // Get teacher details if teacherId is provided
            if (request.containsKey("teacherId")) {
                Object teacherIdObj = request.get("teacherId");
                Long teacherId = null;
                
                if (teacherIdObj instanceof Number) {
                    teacherId = ((Number) teacherIdObj).longValue();
                } else if (teacherIdObj instanceof String && !((String) teacherIdObj).isEmpty()) {
                    teacherId = Long.parseLong((String) teacherIdObj);
                }

                if (teacherId != null) {
                    User teacher = userRepository.findById(teacherId).orElse(null);
                    if (teacher != null) {
                        classEntity.setTeacherId(teacherId);
                        classEntity.setTeacherName(teacher.getName());
                    }
                }
            }

            ClassEntity savedClass = classService.createClass(classEntity);
            return ResponseEntity.ok(savedClass);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating class: " + e.getMessage());
        }
    }

    // UPDATE CLASS
    @PutMapping("/classes/{id}")
    public ResponseEntity<?> updateClass(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            ClassEntity classEntity = new ClassEntity();
            classEntity.setName((String) request.get("name"));
            classEntity.setStartDate((String) request.get("startDate"));
            classEntity.setSchedule((String) request.get("schedule"));
            classEntity.setStatus((String) request.get("status"));
            
            Object feeObj = request.get("fee");
            if (feeObj instanceof Number) {
                classEntity.setFee(((Number) feeObj).doubleValue());
            } else if (feeObj instanceof String) {
                classEntity.setFee(Double.parseDouble((String) feeObj));
            }

            if (request.containsKey("teacherId")) {
                Object teacherIdObj = request.get("teacherId");
                Long teacherId = null;
                
                if (teacherIdObj instanceof Number) {
                    teacherId = ((Number) teacherIdObj).longValue();
                } else if (teacherIdObj instanceof String && !((String) teacherIdObj).isEmpty()) {
                    teacherId = Long.parseLong((String) teacherIdObj);
                }

                if (teacherId != null) {
                    User teacher = userRepository.findById(teacherId).orElse(null);
                    if (teacher != null) {
                        classEntity.setTeacherId(teacherId);
                        classEntity.setTeacherName(teacher.getName());
                    }
                }
            }

            ClassEntity updatedClass = classService.updateClass(id, classEntity);
            return ResponseEntity.ok(updatedClass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating class: " + e.getMessage());
        }
    }

    // DELETE CLASS
    @DeleteMapping("/classes/{id}")
    public ResponseEntity<?> deleteClass(@PathVariable Long id) {
        try {
            classService.deleteClass(id);
            return ResponseEntity.ok(Map.of("message", "Class deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting class: " + e.getMessage());
        }
    }
}