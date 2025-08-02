package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFailValidationWhenEmailIsInvalid() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("email")),
                "Неверный email должен не пройти валидацию");
    }

    @Test
    void shouldFailValidationWhenLoginHasSpaces() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("bad login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("login")),
                "Логин с пробелами должен не пройти валидацию");
    }

    @Test
    void shouldFailValidationWhenEmailIsBlank() {
        User user = new User();
        user.setEmail(" ");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("email")),
                "Пустой email должен не пройти валидацию");
    }

    @Test
    void shouldFailValidationWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin(" ");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("login")),
                "Пустой логин должен не пройти валидацию");
    }

    @Test
    void shouldFailValidationWhenBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.now().plusDays(5));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("birthday")),
                "Будущая дата рождения должна не пройти валидацию");
    }
}
