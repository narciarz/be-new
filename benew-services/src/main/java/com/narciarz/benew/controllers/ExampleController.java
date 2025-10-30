package com.narciarz.benew.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/example")
public class ExampleController {
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }
}
