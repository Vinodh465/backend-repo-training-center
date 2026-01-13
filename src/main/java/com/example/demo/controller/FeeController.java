package com.example.demo.controller;

import com.example.demo.entity.Fee;
import com.example.demo.entity.User;
import com.example.demo.entity.ClassEntity;
import com.example.demo.repository.FeeRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class FeeController {

    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    // GET ALL FEES with student and class information
    @GetMapping("/fees")
    public ResponseEntity<?> getAllFees() {
        try {
            List<Fee> fees = feeRepository.findAll();
            
            List<Map<String, Object>> feeList = fees.stream().map(fee -> {
                Map<String, Object> feeMap = new HashMap<>();
                feeMap.put("id", fee.getId());
                feeMap.put("studentId", fee.getStudentId());
                feeMap.put("classId", fee.getClassId());
                feeMap.put("totalAmount", fee.getTotalAmount());
                feeMap.put("paidAmount", fee.getPaidAmount());
                feeMap.put("dueAmount", fee.getDueAmount());
                feeMap.put("dueDate", fee.getDueDate());
                feeMap.put("status", fee.getStatus());
                feeMap.put("createdAt", fee.getCreatedAt());
                
                // Add student information
                userRepository.findById(fee.getStudentId()).ifPresent(student -> {
                    Map<String, Object> studentInfo = new HashMap<>();
                    studentInfo.put("id", student.getId());
                    studentInfo.put("name", student.getName());
                    studentInfo.put("email", student.getEmail());
                    feeMap.put("student", studentInfo);
                });
                
                // Add class information
                classRepository.findById(fee.getClassId()).ifPresent(cls -> {
                    Map<String, Object> classInfo = new HashMap<>();
                    classInfo.put("id", cls.getId());
                    classInfo.put("name", cls.getName());
                    feeMap.put("class", classInfo);
                });
                
                return feeMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(feeList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching fees: " + e.getMessage());
        }
    }

    // GET FEE BY ID
    @GetMapping("/fees/{id}")
    public ResponseEntity<?> getFeeById(@PathVariable Long id) {
        try {
            Fee fee = feeRepository.findById(id).orElse(null);
            if (fee == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", fee.getId());
            response.put("studentId", fee.getStudentId());
            response.put("classId", fee.getClassId());
            response.put("totalAmount", fee.getTotalAmount());
            response.put("paidAmount", fee.getPaidAmount());
            response.put("dueAmount", fee.getDueAmount());
            response.put("dueDate", fee.getDueDate());
            response.put("status", fee.getStatus());
            
            // Add student and class info
            userRepository.findById(fee.getStudentId()).ifPresent(student -> {
                response.put("studentName", student.getName());
            });
            
            classRepository.findById(fee.getClassId()).ifPresent(cls -> {
                response.put("className", cls.getName());
            });
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching fee: " + e.getMessage());
        }
    }

    // CREATE FEE
    @PostMapping("/fees")
    public ResponseEntity<?> createFee(@RequestBody Map<String, Object> request) {
        try {
            Fee fee = new Fee();
            
            // Parse student ID
            Object studentIdObj = request.get("studentId");
            if (studentIdObj instanceof Number) {
                fee.setStudentId(((Number) studentIdObj).longValue());
            } else if (studentIdObj instanceof String) {
                fee.setStudentId(Long.parseLong((String) studentIdObj));
            }
            
            // Parse class ID
            Object classIdObj = request.get("classId");
            if (classIdObj instanceof Number) {
                fee.setClassId(((Number) classIdObj).longValue());
            } else if (classIdObj instanceof String) {
                fee.setClassId(Long.parseLong((String) classIdObj));
            }
            
            // Parse total amount
            Object totalAmountObj = request.get("totalAmount");
            if (totalAmountObj instanceof Number) {
                fee.setTotalAmount(((Number) totalAmountObj).doubleValue());
            } else if (totalAmountObj instanceof String) {
                fee.setTotalAmount(Double.parseDouble((String) totalAmountObj));
            }
            
            // Set paid amount (default 0)
            fee.setPaidAmount(0.0);
            
            // Calculate due amount
            fee.setDueAmount(fee.getTotalAmount());
            
            // Set due date
            if (request.containsKey("dueDate")) {
                fee.setDueDate((String) request.get("dueDate"));
            }
            
            // Set status
            fee.setStatus("PENDING");
            
            Fee savedFee = feeRepository.save(fee);
            return ResponseEntity.ok(savedFee);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating fee: " + e.getMessage());
        }
    }

    // RECORD PAYMENT
    @PostMapping("/fees/{id}/payment")
    public ResponseEntity<?> recordPayment(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Fee fee = feeRepository.findById(id).orElse(null);
            if (fee == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Parse payment amount
            Object paymentAmountObj = request.get("amount");
            Double paymentAmount = 0.0;
            
            if (paymentAmountObj instanceof Number) {
                paymentAmount = ((Number) paymentAmountObj).doubleValue();
            } else if (paymentAmountObj instanceof String) {
                paymentAmount = Double.parseDouble((String) paymentAmountObj);
            }
            
            // Update paid amount
            fee.setPaidAmount(fee.getPaidAmount() + paymentAmount);
            
            // Update due amount
            fee.setDueAmount(fee.getTotalAmount() - fee.getPaidAmount());
            
            // Update status
            if (fee.getPaidAmount() >= fee.getTotalAmount()) {
                fee.setStatus("PAID");
            } else if (fee.getPaidAmount() > 0) {
                fee.setStatus("PARTIAL");
            }
            
            Fee updatedFee = feeRepository.save(fee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Payment recorded successfully");
            response.put("fee", updatedFee);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error recording payment: " + e.getMessage());
        }
    }

    // UPDATE FEE
    @PutMapping("/fees/{id}")
    public ResponseEntity<?> updateFee(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Fee fee = feeRepository.findById(id).orElse(null);
            if (fee == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Update fields if provided
            if (request.containsKey("totalAmount")) {
                Object totalAmountObj = request.get("totalAmount");
                if (totalAmountObj instanceof Number) {
                    fee.setTotalAmount(((Number) totalAmountObj).doubleValue());
                } else if (totalAmountObj instanceof String) {
                    fee.setTotalAmount(Double.parseDouble((String) totalAmountObj));
                }
            }
            
            if (request.containsKey("dueDate")) {
                fee.setDueDate((String) request.get("dueDate"));
            }
            
            // Recalculate due amount
            fee.setDueAmount(fee.getTotalAmount() - fee.getPaidAmount());
            
            Fee updatedFee = feeRepository.save(fee);
            return ResponseEntity.ok(updatedFee);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating fee: " + e.getMessage());
        }
    }

    // DELETE FEE
    @DeleteMapping("/fees/{id}")
    public ResponseEntity<?> deleteFee(@PathVariable Long id) {
        try {
            feeRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Fee deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error deleting fee: " + e.getMessage());
        }
    }

    // GET FEES BY STUDENT
    @GetMapping("/fees/student/{studentId}")
    public ResponseEntity<?> getFeesByStudent(@PathVariable Long studentId) {
        try {
            List<Fee> fees = feeRepository.findByStudentId(studentId);
            return ResponseEntity.ok(fees);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching student fees: " + e.getMessage());
        }
    }
}