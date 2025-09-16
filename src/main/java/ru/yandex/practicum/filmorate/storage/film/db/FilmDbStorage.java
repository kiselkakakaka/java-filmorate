package ru.yandex.practicum.filmorate.storage.film.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;

    @Override
    public Film add(Film film) {
        final String sql = "INSERT INTO films (name, description, release_date, duration_min, mpa_id) " +
                "VALUES (?,?,?,?,?)";
        KeyHolder kh = new GeneratedKeyHolder();

        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, kh);

        int filmId = Objects.requireNonNull(kh.getKey()).intValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre g : film.getGenres()) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?,?)", filmId, g.getId());
            }
        }

        return getById(filmId).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        final String sql = "UPDATE films SET name=?, description=?, release_date=?, duration_min=?, mpa_id=? WHERE id=?";
        jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        jdbc.update("DELETE FROM film_genres WHERE film_id=?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre g : film.getGenres()) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?,?)", film.getId(), g.getId());
            }
        }

        return getById(film.getId()).orElseThrow();
    }

    @Override
    public Optional<Film> getById(int id) {
        final String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration_min, " +
                "m.id AS mpa_id, m.name AS mpa_name " +     // ВАЖНО: m.name, не label
                "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "WHERE f.id=?";
        List<Film> list = jdbc.query(sql, (rs, rn) -> mapRowToFilm(rs), id);
        if (list.isEmpty()) return Optional.empty();

        Film film = list.get(0);

        // жанры
        final String gsql = "SELECT g.id, g.name " +
                "FROM film_genres fg JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ?";
        Set<Genre> genres = new HashSet<>(jdbc.query(gsql,
                (rs, rn) -> new Genre(rs.getInt("id"), rs.getString("name")), id));
        film.setGenres(genres);

        return Optional.of(film);
    }

    @Override
    public List<Film> getAll() {
        final String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration_min, " +
                "m.id AS mpa_id, m.name AS mpa_name " +     // ВАЖНО: m.name
                "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id";
        return jdbc.query(sql, (rs, rn) -> mapRowToFilm(rs));
    }

    @Override
    public void deleteById(int id) {
        jdbc.update("DELETE FROM films WHERE id=?", id);
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        Film f = new Film();
        f.setId(rs.getInt("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        f.setReleaseDate(rs.getDate("release_date").toLocalDate());
        f.setDuration(rs.getInt("duration_min"));

        MpaRating mpa = new MpaRating(rs.getInt("mpa_id"), rs.getString("mpa_name"));
        f.setMpa(mpa);

        return f;
    }
}
