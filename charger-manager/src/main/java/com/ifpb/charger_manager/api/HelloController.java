package com.ifpb.charger_manager.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifpb.charger_manager.service.HelloService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/hello")
@RequiredArgsConstructor
public class HelloController {

    private final HelloService helloService;

    @GetMapping
    public String get() {
        return helloService.sayHi("kate bush");
    }
}
