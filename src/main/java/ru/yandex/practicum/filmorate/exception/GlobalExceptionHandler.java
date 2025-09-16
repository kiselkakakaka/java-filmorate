package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ErrorResponse body(String message) {
        return new ErrorResponse(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> onMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Ошибка валидации";
        log.warn("Ошибка валидации: {}", msg);
        return new ResponseEntity<>(body(msg), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ValidationException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> onBadRequest(Exception ex) {
        log.warn("Ошибка запроса: {}", ex.getMessage());
        return new ResponseEntity<>(body(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> onConflict(DataIntegrityViolationException ex) {
        log.warn("Нарушение ограничений БД: {}", ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage());
        return new ResponseEntity<>(body("Нарушение ограничений БД (возможно, уже существует email/login)"),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler({NotFoundException.class, java.util.NoSuchElementException.class})
    public ResponseEntity<ErrorResponse> onNotFound(RuntimeException ex) {
        log.warn("Не найдено: {}", ex.getMessage());
        return new ResponseEntity<>(body(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> onOther(Exception ex) {
        log.error("Внутренняя ошибка: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body("Внутренняя ошибка сервера"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
