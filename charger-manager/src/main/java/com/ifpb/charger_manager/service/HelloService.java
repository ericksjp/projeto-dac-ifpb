package com.ifpb.charger_manager.service;

import org.springframework.stereotype.Service;

import com.ifpb.charger_manager.infra.client.MessageClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HelloService {

    private final MessageClient client;

    public String getMessage(Long id) {
        return client.getMessage(id);
    }

    public Long createMessage(String message) {
        return client.createMessage(message);
    }
    
}
