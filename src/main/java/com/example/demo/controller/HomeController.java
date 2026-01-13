package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {
    
    @GetMapping("/")
    public Map<String, String> welcome() {
        Map<String, String> response = new HashMap<>();
        response.put("application", "Training Center Backend");
        response.put("message", "Welcome! API is running");
        response.put("documentation", "/api");
        return response;
    }
}