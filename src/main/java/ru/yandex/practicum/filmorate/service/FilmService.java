package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final FilmLikeStorage filmLikeStorage;
    private final UserStorage userStorage;

    @Transactional
    public Film create(Film film) {
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.add(film);
    }

    @Transactional
    public Film update(Film film) {
        ensureFilmExists(film.getId());
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.update(film);
    }

    @Transactional(readOnly = true)
    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    @Transactional(readOnly = true)
    public Film getById(int id) {
        return filmStorage.getById(id).orElseThrow(
                () -> new NotFoundException("Фильм с ID " + id + " не найден")
        );
    }

    @Transactional
    public void deleteById(int id) {
        ensureFilmExists(id);
        filmStorage.deleteById(id);
    }

    @Transactional
    public void addLike(int filmId, int userId) {
        ensureFilmExists(filmId);
        ensureUserExists(userId);
        filmLikeStorage.addLike(filmId, userId);
    }

    @Transactional
    public void removeLike(int filmId, int userId) {
        ensureFilmExists(filmId);
        ensureUserExists(userId);
        filmLikeStorage.removeLike(filmId, userId);
    }

    @Transactional(readOnly = true)
    public List<Film> getPopular(int count) {
        return filmLikeStorage.findPopular(count);
    }

    private void validateReleaseDate(LocalDate date) {
        if (date == null || date.isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        }
    }

    private void ensureFilmExists(int id) {
        filmStorage.getById(id).orElseThrow(
                () -> new NotFoundException("Фильм с ID " + id + " не найден")
        );
    }

    private void ensureUserExists(int id) {
        userStorage.getById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с ID " + id + " не найден")
        );
    }
}