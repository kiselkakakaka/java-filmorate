package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> likes = new HashMap<>();

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NoSuchElementException("Фильм с ID " + id + " не найден"));
    }

    public void addLike(int filmId, int userId) {
        getById(filmId);
        if (userStorage.getById(userId).isEmpty()) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }
        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    public void removeLike(int filmId, int userId) {
        getById(filmId);
        if (userStorage.getById(userId).isEmpty()) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }
        likes.computeIfAbsent(filmId, k -> new HashSet<>()).remove(userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        likes.getOrDefault(f2.getId(), Collections.emptySet()).size(),
                        likes.getOrDefault(f1.getId(), Collections.emptySet()).size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateReleaseDate(LocalDate date) {
        if (date == null || date.isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        }
    }
}
