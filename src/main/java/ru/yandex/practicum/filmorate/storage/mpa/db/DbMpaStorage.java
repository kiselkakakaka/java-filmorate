package ru.yandex.practicum.filmorate.storage.mpa.db;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.common.MpaRowMapper;
// import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage; // если есть интерфейс

import java.util.List;
import java.util.Optional;

@Repository
public class DbMpaStorage {

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper = new MpaRowMapper();

    public DbMpaStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MpaRating> findAll() {
        final String sql = "SELECT id, name FROM mpa_ratings ORDER BY id";
        return jdbc.query(sql, mapper);
    }

    public Optional<MpaRating> findById(int id) {
        final String sql = "SELECT id, name FROM mpa_ratings WHERE id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, mapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

