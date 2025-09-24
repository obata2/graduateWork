package com.gwork.demo.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiTestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot";
    }

    @PostMapping("/calc")
    public int calc(@RequestBody int x) {
        return x * 2;
    }
}