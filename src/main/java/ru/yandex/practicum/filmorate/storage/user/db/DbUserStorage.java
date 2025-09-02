package ru.yandex.practicum.filmorate.storage.user.db;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.common.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Repository
@Primary
public class DbUserStorage implements UserStorage {

    private final JdbcTemplate jdbc;

    public DbUserStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, kh);
        Number key = kh.getKey();
        user.setId(key == null ? 0 : key.intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbc.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new UserRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT id, email, login, name, birthday FROM users ORDER BY id";
        return jdbc.query(sql, new UserRowMapper());
    }

    @Override
    public void deleteById(int id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }
}

