package com.ifpb.charger_proxy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ifpb.charger_proxy.model.Message;
import com.ifpb.charger_proxy.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    public Message getMessageById(Long id) {
        return messageRepository.get(id);
    }

    @Transactional
    public Long create(String message) {
        return messageRepository.save(message);
    }
}
