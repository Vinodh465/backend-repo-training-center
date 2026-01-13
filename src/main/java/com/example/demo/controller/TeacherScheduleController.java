package com.example.demo.controller;

import com.example.demo.entity.Schedule;
import com.example.demo.entity.ClassEntity;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.repository.ClassRepository;
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
public class TeacherScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ClassRepository classRepository;

    // Get all schedules for a teacher
    @GetMapping("/schedules/{teacherId}")
    public ResponseEntity<?> getTeacherSchedules(@PathVariable Long teacherId) {
        try {
            List<Schedule> schedules = scheduleRepository.findByTeacherId(teacherId);
            
            List<Map<String, Object>> schedulesWithClass = schedules.stream().map(schedule -> {
                Map<String, Object> scheduleMap = new HashMap<>();
                scheduleMap.put("id", schedule.getId());
                scheduleMap.put("classId", schedule.getClassId());
                scheduleMap.put("dayOfWeek", schedule.getDayOfWeek());
                scheduleMap.put("startTime", schedule.getStartTime());
                scheduleMap.put("endTime", schedule.getEndTime());
                scheduleMap.put("room", schedule.getRoom());
                
                // Add class name
                classRepository.findById(schedule.getClassId()).ifPresent(cls -> {
                    scheduleMap.put("className", cls.getName());
                });
                
                return scheduleMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(schedulesWithClass);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching schedules: " + e.getMessage());
        }
    }

    // Create schedule
    @PostMapping("/schedules")
    public ResponseEntity<?> createSchedule(@RequestBody Map<String, Object> request) {
        try {
            Schedule schedule = new Schedule();
            
            // Parse classId
            Object classIdObj = request.get("classId");
            if (classIdObj instanceof Number) {
                schedule.setClassId(((Number) classIdObj).longValue());
            } else if (classIdObj instanceof String) {
                schedule.setClassId(Long.parseLong((String) classIdObj));
            }
            
            // Parse teacherId
            Object teacherIdObj = request.get("teacherId");
            if (teacherIdObj instanceof Number) {
                schedule.setTeacherId(((Number) teacherIdObj).longValue());
            } else if (teacherIdObj instanceof String) {
                schedule.setTeacherId(Long.parseLong((String) teacherIdObj));
            }
            
            schedule.setDayOfWeek((String) request.get("dayOfWeek"));
            schedule.setStartTime((String) request.get("startTime"));
            schedule.setEndTime((String) request.get("endTime"));
            schedule.setRoom((String) request.get("room"));
            
            Schedule savedSchedule = scheduleRepository.save(schedule);
            return ResponseEntity.ok(savedSchedule);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating schedule: " + e.getMessage());
        }
    }

    // Update schedule
    @PutMapping("/schedules/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Schedule schedule = scheduleRepository.findById(id).orElse(null);
            if (schedule == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (request.containsKey("classId")) {
                Object classIdObj = request.get("classId");
                if (classIdObj instanceof Number) {
                    schedule.setClassId(((Number) classIdObj).longValue());
                } else if (classIdObj instanceof String) {
                    schedule.setClassId(Long.parseLong((String) classIdObj));
                }
            }
            
            if (request.containsKey("dayOfWeek")) {
                schedule.setDayOfWeek((String) request.get("dayOfWeek"));
            }
            if (request.containsKey("startTime")) {
                schedule.setStartTime((String) request.get("startTime"));
            }
            if (request.containsKey("endTime")) {
                schedule.setEndTime((String) request.get("endTime"));
            }
            if (request.containsKey("room")) {
                schedule.setRoom((String) request.get("room"));
            }
            
            Schedule updatedSchedule = scheduleRepository.save(schedule);
            return ResponseEntity.ok(updatedSchedule);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating schedule: " + e.getMessage());
        }
    }

    // Delete schedule
    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Schedule deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error deleting schedule: " + e.getMessage());
        }
    }
}