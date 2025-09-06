package ru.yandex.practicum.filmorate.storage.like.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.like.FilmLikeStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class DbFilmLikeStorage implements FilmLikeStorage {

    private final JdbcTemplate jdbc;

    @Override
    public void addLike(int filmId, int userId) {
        final String sql = "MERGE INTO film_likes (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbc.update("DELETE FROM film_likes WHERE film_id=? AND user_id=?", filmId, userId);
    }

    @Override
    public List<Film> findPopular(int count) {
        final String sql = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration_min,
                   m.id AS mpa_id, m.name AS mpa_name,
                   COALESCE(l.cnt, 0) AS likes_cnt
              FROM films f
              JOIN mpa_ratings m ON m.id = f.mpa_id
              LEFT JOIN (
                   SELECT film_id, COUNT(*) AS cnt
                     FROM film_likes
                    GROUP BY film_id
              ) l ON l.film_id = f.id
             ORDER BY likes_cnt DESC, f.id
             FETCH FIRST ? ROWS ONLY
            """;

        List<Film> films = jdbc.query(sql, (rs, rn) -> mapFilm(rs), count);
        if (films.isEmpty()) return films;

        Map<Integer, LinkedHashSet<Genre>> byFilm = loadGenresForFilms(films);
        for (Film f : films) {
            f.setGenres(byFilm.getOrDefault(f.getId(), new LinkedHashSet<>()));
        }
        return films;
    }

    private Film mapFilm(ResultSet rs) throws SQLException {
        Film f = new Film();
        f.setId(rs.getInt("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        LocalDate rd = rs.getObject("release_date", LocalDate.class);
        f.setReleaseDate(rd);
        f.setDuration(rs.getInt("duration_min"));
        f.setMpa(new MpaRating(
                rs.getInt("mpa_id"),
                rs.getString("mpa_name")
        ));
        return f;
    }

    private Map<Integer, LinkedHashSet<Genre>> loadGenresForFilms(List<Film> films) {
        String placeholders = String.join(",",
                Collections.nCopies(films.size(), "?"));
        String gsql = """
            SELECT fg.film_id, g.id, g.name
              FROM film_genres fg
              JOIN genres g ON g.id = fg.genre_id
             WHERE fg.film_id IN (""" + placeholders + ") ORDER BY fg.film_id, g.id";

        Object[] ids = films.stream().map(Film::getId).toArray();
        Map<Integer, LinkedHashSet<Genre>> byFilm = new HashMap<>();
        jdbc.query(gsql, rs -> {
            int filmId = rs.getInt("film_id");
            byFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                    .add(new Genre(rs.getInt("id"), rs.getString("name")));
        }, ids);
        return byFilm;
    }
}