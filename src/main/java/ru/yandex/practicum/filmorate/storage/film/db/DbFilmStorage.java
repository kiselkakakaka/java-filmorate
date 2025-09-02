package ru.yandex.practicum.filmorate.storage.film.db;

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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.common.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

@Repository
@Primary
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbc;

    public DbFilmStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Film add(Film film) {
        String sql =
                "INSERT INTO films (name, description, release_date, duration_min, mpa_id) " +
                        "VALUES (?, ?, ?, ?, (SELECT id FROM mpa_ratings WHERE code = 'PG-13'))";
        // ↑ временно ставим PG-13, т.к. в твоей модели пока нет поля mpa.
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            return ps;
        }, kh);
        Number key = kh.getKey();
        film.setId(key == null ? 0 : key.intValue());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql =
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration_min = ? WHERE id = ?";
        jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId()
        );
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        String sql =
                "SELECT f.id, f.name, f.description, f.release_date, f.duration_min " +
                        "FROM films f WHERE f.id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new FilmRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAll() {
        String sql =
                "SELECT f.id, f.name, f.description, f.release_date, f.duration_min " +
                        "FROM films f ORDER BY f.id";
        return jdbc.query(sql, new FilmRowMapper());
    }

    @Override
    public void deleteById(int id) {
        jdbc.update("DELETE FROM films WHERE id = ?", id);
    }
}

