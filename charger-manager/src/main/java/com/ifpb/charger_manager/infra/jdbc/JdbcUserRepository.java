package com.ifpb.charger_manager.infra.jdbc;

import com.ifpb.charger_manager.domain.model.User;
import com.ifpb.charger_manager.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .email(rs.getString("email"))
            .cpfCnpj(rs.getString("cpf_cnpj"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (name, email, cpf_cnpj, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getCpfCnpj());
            ps.setTimestamp(4, Timestamp.valueOf(now));
            ps.setTimestamp(5, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);
        
        user.setId(keyHolder.getKey().longValue());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, cpf_cnpj = ?, updated_at = ? " +
                     "WHERE id = ?";
        
        LocalDateTime now = LocalDateTime.now();
        
        jdbcTemplate.update(sql,
                user.getName(),
                user.getEmail(),
                user.getCpfCnpj(),
                Timestamp.valueOf(now),
                user.getId()
        );
        
        user.setUpdatedAt(now);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByCpfCnpj(String cpfCnpj) {
        String sql = "SELECT COUNT(*) FROM users WHERE cpf_cnpj = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cpfCnpj);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email, id);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByCpfCnpjAndIdNot(String cpfCnpj, Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE cpf_cnpj = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cpfCnpj, id);
        return count != null && count > 0;
    }
}
