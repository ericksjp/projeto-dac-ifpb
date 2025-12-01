package com.ifpb.charger_manager.service;

import org.springframework.stereotype.Service;

import com.ifpb.charger_manager.infra.client.HelloClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HelloService {

    private final HelloClient client;

    public String sayHi(String person) {
        return client.sayHi(person);
    }
    
}
