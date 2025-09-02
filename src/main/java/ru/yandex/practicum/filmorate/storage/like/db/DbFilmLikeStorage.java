package ru.yandex.practicum.filmorate.storage.like.db;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.common.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.like.FilmLikeStorage;

@Repository
public class DbFilmLikeStorage implements FilmLikeStorage {

    private final JdbcTemplate jdbc;

    public DbFilmLikeStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?) " +
                "ON CONFLICT (film_id, user_id) DO NOTHING";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbc.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> findPopular(int limit) {
        String sql =
                "SELECT f.id, f.name, f.description, f.release_date, f.duration_min " +
                        "FROM films f " +
                        "LEFT JOIN film_likes fl ON fl.film_id = f.id " +
                        "GROUP BY f.id, f.name, f.description, f.release_date, f.duration_min " +
                        "ORDER BY COUNT(fl.user_id) DESC, f.id " +
                        "LIMIT ?";
        return jdbc.query(sql, new FilmRowMapper(), limit);
    }
}

