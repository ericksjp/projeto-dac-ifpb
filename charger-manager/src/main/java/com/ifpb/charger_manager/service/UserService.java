package com.ifpb.charger_manager.service;

import com.ifpb.charger_manager.api.dto.UserCreateDto;
import com.ifpb.charger_manager.api.dto.UserResponseDto;
import com.ifpb.charger_manager.api.dto.UserUpdateDto;
import com.ifpb.charger_manager.domain.model.User;
import com.ifpb.charger_manager.domain.repository.UserRepository;
import com.ifpb.charger_manager.exception.DuplicateResourceException;
import com.ifpb.charger_manager.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDto createUser(UserCreateDto createDto) {
        
        if (userRepository.existsByEmail(createDto.getEmail())) {
            throw new DuplicateResourceException("Email já cadastrado: " + createDto.getEmail());
        }
        if (userRepository.existsByCpfCnpj(createDto.getCpfCnpj())) {
            throw new DuplicateResourceException("CPF/CNPJ já cadastrado: " + createDto.getCpfCnpj());
        }

        User user = User.builder()
                .name(createDto.getName())
                .email(createDto.getEmail())
                .cpfCnpj(createDto.getCpfCnpj())
                .build();

        User savedUser = userRepository.save(user);
        return toResponseDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setName(updateDto.getName());

        User updatedUser = userRepository.update(user);
        return toResponseDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .cpfCnpj(user.getCpfCnpj())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
