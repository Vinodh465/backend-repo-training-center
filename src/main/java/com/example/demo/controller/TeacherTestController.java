package com.example.demo.controller;

import com.example.demo.entity.Test;
import com.example.demo.entity.TestResult;
import com.example.demo.entity.ClassEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.TestRepository;
import com.example.demo.repository.TestResultRepository;
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
public class TeacherTestController {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all tests for a teacher
    @GetMapping("/tests/{teacherId}")
    public ResponseEntity<?> getTeacherTests(@PathVariable Long teacherId) {
        try {
            List<Test> tests = testRepository.findByTeacherId(teacherId);
            
            List<Map<String, Object>> testsWithDetails = tests.stream().map(test -> {
                Map<String, Object> testMap = new HashMap<>();
                testMap.put("id", test.getId());
                testMap.put("title", test.getTitle());
                testMap.put("classId", test.getClassId());
                testMap.put("testDate", test.getTestDate());
                testMap.put("duration", test.getDuration());
                testMap.put("type", test.getType());
                testMap.put("description", test.getDescription());
                testMap.put("totalMarks", test.getTotalMarks());
                testMap.put("questions", test.getQuestions());
                testMap.put("status", test.getStatus());
                testMap.put("createdAt", test.getCreatedAt());
                
                // Add class name
                classRepository.findById(test.getClassId()).ifPresent(cls -> {
                    testMap.put("className", cls.getName());
                });
                
                return testMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(testsWithDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching tests: " + e.getMessage());
        }
    }

    // Create test
    @PostMapping("/tests")
    public ResponseEntity<?> createTest(@RequestBody Map<String, Object> request) {
        try {
            Test test = new Test();
            
            test.setTitle((String) request.get("title"));
            
            // Parse classId
            Object classIdObj = request.get("classId");
            if (classIdObj instanceof Number) {
                test.setClassId(((Number) classIdObj).longValue());
            } else if (classIdObj instanceof String) {
                test.setClassId(Long.parseLong((String) classIdObj));
            }
            
            // Parse teacherId
            Object teacherIdObj = request.get("teacherId");
            if (teacherIdObj instanceof Number) {
                test.setTeacherId(((Number) teacherIdObj).longValue());
            } else if (teacherIdObj instanceof String) {
                test.setTeacherId(Long.parseLong((String) teacherIdObj));
            }
            
            test.setTestDate((String) request.get("testDate"));
            
            // Parse duration
            Object durationObj = request.get("duration");
            if (durationObj instanceof Number) {
                test.setDuration(((Number) durationObj).intValue());
            } else if (durationObj instanceof String) {
                test.setDuration(Integer.parseInt((String) durationObj));
            }
            
            test.setType((String) request.get("type"));
            test.setDescription((String) request.get("description"));
            
            // Parse totalMarks
            Object totalMarksObj = request.get("totalMarks");
            if (totalMarksObj instanceof Number) {
                test.setTotalMarks(((Number) totalMarksObj).intValue());
            } else if (totalMarksObj instanceof String) {
                test.setTotalMarks(Integer.parseInt((String) totalMarksObj));
            }
            
            // Store questions as JSON string
            test.setQuestions(request.get("questions").toString());
            
            test.setStatus("Scheduled");
            
            Test savedTest = testRepository.save(test);
            return ResponseEntity.ok(savedTest);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating test: " + e.getMessage());
        }
    }

    // Update test
    @PutMapping("/tests/{id}")
    public ResponseEntity<?> updateTest(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Test test = testRepository.findById(id).orElse(null);
            if (test == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (request.containsKey("title")) {
                test.setTitle((String) request.get("title"));
            }
            if (request.containsKey("testDate")) {
                test.setTestDate((String) request.get("testDate"));
            }
            if (request.containsKey("duration")) {
                Object durationObj = request.get("duration");
                if (durationObj instanceof Number) {
                    test.setDuration(((Number) durationObj).intValue());
                }
            }
            if (request.containsKey("type")) {
                test.setType((String) request.get("type"));
            }
            if (request.containsKey("description")) {
                test.setDescription((String) request.get("description"));
            }
            if (request.containsKey("questions")) {
                test.setQuestions(request.get("questions").toString());
            }
            if (request.containsKey("status")) {
                test.setStatus((String) request.get("status"));
            }
            
            Test updatedTest = testRepository.save(test);
            return ResponseEntity.ok(updatedTest);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating test: " + e.getMessage());
        }
    }

    // Delete test
    @DeleteMapping("/tests/{id}")
    public ResponseEntity<?> deleteTest(@PathVariable Long id) {
        try {
            // Delete all results for this test first
            List<TestResult> results = testResultRepository.findByTestId(id);
            testResultRepository.deleteAll(results);
            
            // Delete the test
            testRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Test deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error deleting test: " + e.getMessage());
        }
    }

    // Get test results for a specific test
    @GetMapping("/tests/{testId}/results")
    public ResponseEntity<?> getTestResults(@PathVariable Long testId) {
        try {
            List<TestResult> results = testResultRepository.findByTestId(testId);
            
            List<Map<String, Object>> resultsWithDetails = results.stream().map(result -> {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("id", result.getId());
                resultMap.put("testId", result.getTestId());
                resultMap.put("studentId", result.getStudentId());
                resultMap.put("score", result.getScore());
                resultMap.put("totalMarks", result.getTotalMarks());
                resultMap.put("answers", result.getAnswers());
                resultMap.put("submittedAt", result.getSubmittedAt());
                
                // Calculate percentage and grade
                double percentage = (result.getScore() * 100.0) / result.getTotalMarks();
                resultMap.put("percentage", String.format("%.1f", percentage));
                resultMap.put("grade", getGrade(percentage));
                
                // Add student name
                userRepository.findById(result.getStudentId()).ifPresent(student -> {
                    resultMap.put("studentName", student.getName());
                    resultMap.put("studentEmail", student.getEmail());
                });
                
                return resultMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(resultsWithDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching results: " + e.getMessage());
        }
    }

    // Get all results for teacher's tests
    @GetMapping("/results/{teacherId}")
    public ResponseEntity<?> getAllResults(@PathVariable Long teacherId) {
        try {
            List<Test> tests = testRepository.findByTeacherId(teacherId);
            List<Long> testIds = tests.stream().map(Test::getId).collect(Collectors.toList());
            
            List<TestResult> allResults = testResultRepository.findAll().stream()
                .filter(result -> testIds.contains(result.getTestId()))
                .collect(Collectors.toList());
            
            List<Map<String, Object>> resultsWithDetails = allResults.stream().map(result -> {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("id", result.getId());
                resultMap.put("testId", result.getTestId());
                resultMap.put("studentId", result.getStudentId());
                resultMap.put("score", result.getScore());
                resultMap.put("totalMarks", result.getTotalMarks());
                resultMap.put("submittedAt", result.getSubmittedAt());
                
                double percentage = (result.getScore() * 100.0) / result.getTotalMarks();
                resultMap.put("percentage", String.format("%.1f", percentage));
                resultMap.put("grade", getGrade(percentage));
                
                // Add test name
                testRepository.findById(result.getTestId()).ifPresent(test -> {
                    resultMap.put("testTitle", test.getTitle());
                    
                    // Add class name
                    classRepository.findById(test.getClassId()).ifPresent(cls -> {
                        resultMap.put("className", cls.getName());
                    });
                });
                
                // Add student name
                userRepository.findById(result.getStudentId()).ifPresent(student -> {
                    resultMap.put("studentName", student.getName());
                });
                
                return resultMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(resultsWithDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching results: " + e.getMessage());
        }
    }

    private String getGrade(double percentage) {
        if (percentage >= 90) return "Excellent";
        if (percentage >= 75) return "Very Good";
        if (percentage >= 60) return "Good";
        if (percentage >= 40) return "Need to Improve";
        return "Poor";
    }
}