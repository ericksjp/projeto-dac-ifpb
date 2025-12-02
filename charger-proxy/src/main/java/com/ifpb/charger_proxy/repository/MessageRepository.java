package com.ifpb.charger_proxy.repository;

import com.ifpb.charger_proxy.model.Message;

public interface MessageRepository {
    Message get(Long id);
    Long save(String message);
}
