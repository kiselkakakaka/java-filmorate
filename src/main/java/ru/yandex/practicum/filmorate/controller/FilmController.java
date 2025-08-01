package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final List<Film> films = new ArrayList<>();
    private int nextId = 1;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @PostMapping
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.add(film);
        log.info("Создан фильм: {}", film);
        return ResponseEntity.status(201).body(film);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        validateFilm(film);
        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getId() == film.getId()) {
                films.set(i, film);
                log.info("Фильм обновлён: {}", film);
                return ResponseEntity.ok(film);
            }
        }
        log.warn("Фильм с id={} не найден для обновления", film.getId());
        throw new NoSuchElementException("Фильм с ID " + film.getId() + " не найден");
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return films;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new IllegalArgumentException("Дата релиза не может быть раньше 28.12.1895");
        }
    }
}
