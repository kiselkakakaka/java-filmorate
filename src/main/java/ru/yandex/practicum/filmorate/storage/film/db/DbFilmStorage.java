package ru.yandex.practicum.filmorate.storage.film.db;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;


@Repository
@Primary
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbc;

    public DbFilmStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    @Override
    public Film add(Film film) {
        final String sql = """
            INSERT INTO films (name, description, release_date, duration_min, mpa_id)
            VALUES (?, ?, ?, ?, ?)
            """;

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate()); // LocalDate -> DATE
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, kh);

        int filmId = kh.getKey() == null ? 0 : kh.getKey().intValue();

        // жанры
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre g : film.getGenres()) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", filmId, g.getId());
            }
        }

        return getById(filmId).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        final String sql = """
            UPDATE films
               SET name = ?, description = ?, release_date = ?, duration_min = ?, mpa_id = ?
             WHERE id = ?
            """;

        jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre g : film.getGenres()) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", film.getId(), g.getId());
            }
        }

        return getById(film.getId()).orElseThrow();
    }

    @Override
    public Optional<Film> getById(int id) {
        final String sql = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration_min,
                   m.id AS mpa_id, m.name AS mpa_name
              FROM films f
              JOIN mpa_ratings m ON m.id = f.mpa_id
             WHERE f.id = ?
            """;

        List<Film> list = jdbc.query(sql, (rs, rn) -> mapRowToFilm(rs), id);
        if (list.isEmpty()) return Optional.empty();

        Film film = list.get(0);
        film.setGenres(loadGenresForFilm(film.getId()));

        return Optional.of(film);
    }

    @Override
    public List<Film> getAll() {
        final String sql = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration_min,
                   m.id AS mpa_id, m.name AS mpa_name
              FROM films f
              JOIN mpa_ratings m ON m.id = f.mpa_id
             ORDER BY f.id
            """;

        List<Film> films = jdbc.query(sql, (rs, rn) -> mapRowToFilm(rs));

        if (films.isEmpty()) return films;

        final String gsql = """
            SELECT fg.film_id, g.id, g.name
              FROM film_genres fg
              JOIN genres g ON g.id = fg.genre_id
             WHERE fg.film_id IN (%s)
             ORDER BY fg.film_id, g.id
            """;

        String in = films.stream().map(f -> "?").reduce((a, b) -> a + "," + b).orElse("?");
        String finalSql = gsql.formatted(in);
        Object[] ids = films.stream().map(Film::getId).toArray();

        Map<Integer, LinkedHashSet<Genre>> byFilm = new HashMap<>();
        jdbc.query(finalSql, rs -> {
            int filmId = rs.getInt("film_id");
            byFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                    .add(new Genre(rs.getInt("id"), rs.getString("name")));
        }, ids);

        for (Film f : films) {
            f.setGenres(byFilm.getOrDefault(f.getId(), new LinkedHashSet<>()));
        }

        return films;
    }

    @Override
    public void deleteById(int id) {
        jdbc.update("DELETE FROM films WHERE id = ?", id);
    }


    private Film mapRowToFilm(ResultSet rs) throws SQLException {
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

    private LinkedHashSet<Genre> loadGenresForFilm(int filmId) {
        final String sql = """
            SELECT g.id, g.name
              FROM film_genres fg
              JOIN genres g ON g.id = fg.genre_id
             WHERE fg.film_id = ?
             ORDER BY g.id
            """;
        return new LinkedHashSet<>(jdbc.query(sql,
                (rs, rn) -> new Genre(rs.getInt("id"), rs.getString("name")),
                filmId));
    }
}
