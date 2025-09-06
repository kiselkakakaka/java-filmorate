package ru.yandex.practicum.filmorate.storage.genre.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.common.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class DbGenreStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    public DbGenreStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.mapper = new GenreRowMapper();
    }

    public List<Genre> findAll() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbc.query(sql, mapper);
    }

    public Optional<Genre> findById(int id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        return jdbc.query(sql, mapper, id).stream().findFirst();
    }
}

