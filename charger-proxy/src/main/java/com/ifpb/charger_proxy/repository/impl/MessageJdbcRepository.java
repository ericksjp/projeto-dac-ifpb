package com.ifpb.charger_proxy.repository.impl;

import java.util.Collections;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ifpb.charger_proxy.model.Message;
import com.ifpb.charger_proxy.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MessageJdbcRepository implements MessageRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Message get(Long id) {
        var message = jdbcTemplate.query(
                "SELECT * FROM messages WHERE id=?",
                BeanPropertyRowMapper.newInstance(Message.class),
                id);

        if (message.size() == 0) return null;

        return message.get(0);
    }

    @Override
    public Long save(String message) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("messages")
            .usingGeneratedKeyColumns("id");

        Long id = insert.executeAndReturnKey(Collections.singletonMap("content", message)).longValue();

        return id;
    }
}
