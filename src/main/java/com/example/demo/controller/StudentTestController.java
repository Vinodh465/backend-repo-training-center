package com.example.demo.controller;

import com.example.demo.entity.Test;
import com.example.demo.entity.TestResult;
import com.example.demo.entity.User;
import com.example.demo.repository.TestRepository;
import com.example.demo.repository.TestResultRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentTestController {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    // Get available tests for student
    @GetMapping("/tests/{studentId}")
    public ResponseEntity<?> getAvailableTests(@PathVariable Long studentId) {
        try {
            User student = userRepository.findById(studentId).orElse(null);
            if (student == null || student.getClassId() == null) {
                return ResponseEntity.ok(List.of());
            }
            
            List<Test> classTests = testRepository.findByClassId(student.getClassId());
            
            List<Map<String, Object>> testsWithStatus = classTests.stream().map(test -> {
                Map<String, Object> testMap = new HashMap<>();
                testMap.put("id", test.getId());
                testMap.put("title", test.getTitle());
                testMap.put("testDate", test.getTestDate());
                testMap.put("duration", test.getDuration());
                testMap.put("type", test.getType());
                testMap.put("description", test.getDescription());
                testMap.put("totalMarks", test.getTotalMarks());
                testMap.put("status", test.getStatus());
                
                // Add class name
                classRepository.findById(test.getClassId()).ifPresent(cls -> {
                    testMap.put("className", cls.getName());
                });
                
                // Check if student has already taken this test
                Optional<TestResult> existingResult = testResultRepository.findByTestIdAndStudentId(test.getId(), studentId);
                testMap.put("alreadyTaken", existingResult.isPresent());
                
                if (existingResult.isPresent()) {
                    TestResult result = existingResult.get();
                    testMap.put("score", result.getScore());
                    testMap.put("submittedAt", result.getSubmittedAt());
                }
                
                // Only include questions if not already taken
                if (!existingResult.isPresent()) {
                    testMap.put("questions", test.getQuestions());
                }
                
                return testMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(testsWithStatus);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching tests: " + e.getMessage());
        }
    }

    // Submit test
    @PostMapping("/tests/submit")
    public ResponseEntity<?> submitTest(@RequestBody Map<String, Object> request) {
        try {
            // Parse testId
            Object testIdObj = request.get("testId");
            Long testId = null;
            if (testIdObj instanceof Number) {
                testId = ((Number) testIdObj).longValue();
            } else if (testIdObj instanceof String) {
                testId = Long.parseLong((String) testIdObj);
            }
            
            // Parse studentId
            Object studentIdObj = request.get("studentId");
            Long studentId = null;
            if (studentIdObj instanceof Number) {
                studentId = ((Number) studentIdObj).longValue();
            } else if (studentIdObj instanceof String) {
                studentId = Long.parseLong((String) studentIdObj);
            }
            
            // Check if already submitted
            Optional<TestResult> existingResult = testResultRepository.findByTestIdAndStudentId(testId, studentId);
            if (existingResult.isPresent()) {
                return ResponseEntity.badRequest().body("Test already submitted");
            }
            
            // Parse score
            Object scoreObj = request.get("score");
            Integer score = null;
            if (scoreObj instanceof Number) {
                score = ((Number) scoreObj).intValue();
            } else if (scoreObj instanceof String) {
                score = Integer.parseInt((String) scoreObj);
            }
            
            // Parse totalMarks
            Object totalMarksObj = request.get("totalMarks");
            Integer totalMarks = null;
            if (totalMarksObj instanceof Number) {
                totalMarks = ((Number) totalMarksObj).intValue();
            } else if (totalMarksObj instanceof String) {
                totalMarks = Integer.parseInt((String) totalMarksObj);
            }
            
            TestResult result = new TestResult();
            result.setTestId(testId);
            result.setStudentId(studentId);
            result.setScore(score);
            result.setTotalMarks(totalMarks);
            result.setAnswers(request.get("answers").toString());
            
            TestResult savedResult = testResultRepository.save(result);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test submitted successfully");
            response.put("result", savedResult);
            response.put("percentage", (score * 100.0) / totalMarks);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error submitting test: " + e.getMessage());
        }
    }

    // Get student's test results
    @GetMapping("/results/{studentId}")
    public ResponseEntity<?> getStudentResults(@PathVariable Long studentId) {
        try {
            List<TestResult> results = testResultRepository.findByStudentId(studentId);
            
            List<Map<String, Object>> resultsWithDetails = results.stream().map(result -> {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("id", result.getId());
                resultMap.put("testId", result.getTestId());
                resultMap.put("score", result.getScore());
                resultMap.put("totalMarks", result.getTotalMarks());
                resultMap.put("submittedAt", result.getSubmittedAt());
                
                double percentage = (result.getScore() * 100.0) / result.getTotalMarks();
                resultMap.put("percentage", String.format("%.1f", percentage));
                resultMap.put("grade", getGrade(percentage));
                
                // Add test details
                testRepository.findById(result.getTestId()).ifPresent(test -> {
                    resultMap.put("testTitle", test.getTitle());
                    resultMap.put("testDate", test.getTestDate());
                    
                    classRepository.findById(test.getClassId()).ifPresent(cls -> {
                        resultMap.put("className", cls.getName());
                    });
                });
                
                return resultMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(resultsWithDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching results: " + e.getMessage());
        }
    }

    // Get student progress data
    @GetMapping("/progress/{studentId}")
    public ResponseEntity<?> getStudentProgress(@PathVariable Long studentId) {
        try {
            List<TestResult> results = testResultRepository.findByStudentId(studentId);
            
            Map<String, Object> progressData = new HashMap<>();
            progressData.put("totalTests", results.size());
            
            if (results.isEmpty()) {
                progressData.put("averageScore", 0);
                progressData.put("gradeDistribution", Map.of(
                    "Poor", 0,
                    "Need to Improve", 0,
                    "Good", 0,
                    "Very Good", 0,
                    "Excellent", 0
                ));
                return ResponseEntity.ok(progressData);
            }
            
            // Calculate average
            double totalPercentage = 0;
            Map<String, Integer> gradeDistribution = new HashMap<>();
            gradeDistribution.put("Poor", 0);
            gradeDistribution.put("Need to Improve", 0);
            gradeDistribution.put("Good", 0);
            gradeDistribution.put("Very Good", 0);
            gradeDistribution.put("Excellent", 0);
            
            for (TestResult result : results) {
                double percentage = (result.getScore() * 100.0) / result.getTotalMarks();
                totalPercentage += percentage;
                
                String grade = getGrade(percentage);
                gradeDistribution.put(grade, gradeDistribution.get(grade) + 1);
            }
            
            double averageScore = totalPercentage / results.size();
            progressData.put("averageScore", String.format("%.1f", averageScore));
            progressData.put("currentGrade", getGrade(averageScore));
            progressData.put("gradeDistribution", gradeDistribution);
            
            // Performance trend
            List<Map<String, Object>> trend = results.stream().map(result -> {
                Map<String, Object> point = new HashMap<>();
                testRepository.findById(result.getTestId()).ifPresent(test -> {
                    point.put("testTitle", test.getTitle());
                });
                double percentage = (result.getScore() * 100.0) / result.getTotalMarks();
                point.put("percentage", String.format("%.1f", percentage));
                return point;
            }).collect(Collectors.toList());
            
            progressData.put("trend", trend);
            
            return ResponseEntity.ok(progressData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching progress: " + e.getMessage());
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