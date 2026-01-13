package com.example.demo.repository;

import com.example.demo.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByTestId(Long testId);
    List<TestResult> findByStudentId(Long studentId);
    Optional<TestResult> findByTestIdAndStudentId(Long testId, Long studentId);
}