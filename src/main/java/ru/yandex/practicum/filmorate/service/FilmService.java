package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.FilmLikeStorage;

@Service
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final FilmLikeStorage filmLikeStorage;

    public FilmService(FilmStorage filmStorage, FilmLikeStorage filmLikeStorage) {
        this.filmStorage = filmStorage;
        this.filmLikeStorage = filmLikeStorage;
    }

    public Film create(Film film) {
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.add(film);   // делегируем на add(...)
    }

    public Film update(Film film) {
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return filmStorage.getById(id).orElseThrow(
                () -> new NotFoundException("Фильм с ID " + id + " не найден")
        );
    }

    public void deleteById(int id) {
        filmStorage.deleteById(id);
    }

    public void addLike(int filmId, int userId) {
        filmLikeStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        filmLikeStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(int count) {
        return filmLikeStorage.findPopular(count);
    }

    private void validateReleaseDate(LocalDate date) {
        if (date == null || date.isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        }
    }
}
