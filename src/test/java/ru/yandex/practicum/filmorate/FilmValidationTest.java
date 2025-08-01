package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        Film film = new Film();
        film.setName(" ");
        film.setDescription("Хороший фильм");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setDuration(-50);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }
}

