package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private Map<String, String> body(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("error", message);
        return m;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> onMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Ошибка валидации";
        log.warn("Ошибка валидации: {}", msg);
        return new ResponseEntity<>(body(msg), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ValidationException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> onBadRequest(Exception ex) {
        log.warn("Ошибка запроса: {}", ex.getMessage());
        return new ResponseEntity<>(body(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> onConflict(DataIntegrityViolationException ex) {
        log.warn("Нарушение ограничений БД: {}", ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage());
        // обычно это дубликаты email/login или FK-конфликт
        return new ResponseEntity<>(body("Нарушение ограничений БД (возможно, уже существует email/login)"),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler({NotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<Map<String, String>> onNotFound(RuntimeException ex) {
        log.warn("Не найдено: {}", ex.getMessage());
        return new ResponseEntity<>(body(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> onOther(Exception ex) {
        log.error("Внутренняя ошибка: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body("Внутренняя ошибка сервера"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}