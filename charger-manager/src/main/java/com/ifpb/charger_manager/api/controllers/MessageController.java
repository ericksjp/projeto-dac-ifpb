package com.ifpb.charger_manager.api.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ifpb.charger_manager.api.dto.MessageCreateDto;
import com.ifpb.charger_manager.service.HelloService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final HelloService helloService;

    @GetMapping("/{id}")
    public String getMessage(@PathVariable Long id) {
        return helloService.getMessage(id);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody MessageCreateDto message) {
        if (message == null) {
            return ResponseEntity.badRequest().body("message cannot be null");
        }

        System.out.println(message.message());

        Long id = helloService.createMessage(message.message());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).body("message created with ID: " + id);
    }
}
