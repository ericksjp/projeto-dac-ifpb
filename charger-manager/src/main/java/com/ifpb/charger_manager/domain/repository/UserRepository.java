package com.ifpb.charger_manager.domain.repository;

import com.ifpb.charger_manager.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    
    User save(User user);
    
    User update(User user);
    
    Optional<User> findById(Long id);
    
    List<User> findAll();
    
    void deleteById(Long id);
    
    boolean existsByEmail(String email);
    
    boolean existsByCpfCnpj(String cpfCnpj);
    
    boolean existsByEmailAndIdNot(String email, Long id);
    
    boolean existsByCpfCnpjAndIdNot(String cpfCnpj, Long id);
}
