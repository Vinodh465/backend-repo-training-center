package com.example.demo.repository;

import com.example.demo.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    
    // Find fees by student
    List<Fee> findByStudentId(Long studentId);
    
    // Find fees by class
    List<Fee> findByClassId(Long classId);
    
    // Find fee by student and class
    Optional<Fee> findByStudentIdAndClassId(Long studentId, Long classId);
    
    // Find fees by status
    List<Fee> findByStatus(String status);
}