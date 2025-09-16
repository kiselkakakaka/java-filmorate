package ru.yandex.practicum.filmorate.storage.film.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
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
        final String sql = """
            INSERT INTO films (name, description, release_date, duration_min, mpa_id)
            VALUES (?, ?, ?, ?, ?)
            """;

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, kh);

        int filmId = Objects.requireNonNull(kh.getKey()).intValue();

        saveFilmGenresBatch(filmId, film.getGenres());

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
        saveFilmGenresBatch(film.getId(), film.getGenres());

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

        Map<Integer, LinkedHashSet<Genre>> byFilm = loadGenresForFilms(list);
        Film film = list.get(0);
        film.setGenres(byFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));

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

        Map<Integer, LinkedHashSet<Genre>> byFilm = loadGenresForFilms(films);
        for (Film f : films) {
            f.setGenres(byFilm.getOrDefault(f.getId(), new LinkedHashSet<>()));
        }
        return films;
    }

    @Override
    public void deleteById(int id) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", id);
        jdbc.update("DELETE FROM film_likes WHERE film_id = ?", id);
        jdbc.update("DELETE FROM films WHERE id = ?", id);
    }

    private void saveFilmGenresBatch(int filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        List<Integer> ids = genres.stream()
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());

        jdbc.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                ids,
                200,
                (ps, genreId) -> {
                    ps.setInt(1, filmId);
                    ps.setInt(2, genreId);
                }
        );
    }

    public Map<Integer, LinkedHashSet<Genre>> loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return Map.of();

        String placeholders = films.stream().map(f -> "?").collect(Collectors.joining(","));
        final String gsql = """
            SELECT fg.film_id, g.id, g.name
              FROM film_genres fg
              JOIN genres g ON g.id = fg.genre_id
             WHERE fg.film_id IN (%s)
             ORDER BY fg.film_id, g.id
            """.formatted(placeholders);

        Object[] ids = films.stream().map(Film::getId).toArray();

        Map<Integer, LinkedHashSet<Genre>> byFilm = new LinkedHashMap<>();
        jdbc.query(gsql, rs -> {
            int filmId = rs.getInt("film_id");
            byFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                    .add(new Genre(rs.getInt("id"), rs.getString("name")));
        }, ids);
        return byFilm;
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        Film f = new Film();
        f.setId(rs.getInt("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        LocalDate rd = rs.getObject("release_date", LocalDate.class);
        f.setReleaseDate(rd);
        f.setDuration(rs.getInt("duration_min"));
        f.setMpa(new MpaRating(rs.getInt("mpa_id"), rs.getString("mpa_name")));
        return f;
    }
}
