package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {
    
    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Training Center Backend API is running successfully!");
        response.put("status", "success");
        response.put("version", "1.0");
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("database", "Connected");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}