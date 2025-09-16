package ru.yandex.practicum.filmorate.controller;

import java.util.List;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

@Validated
@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final MpaService service;

    public MpaController(MpaService service) {
        this.service = service;
    }

    @GetMapping
    public List<MpaRating> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public MpaRating getById(@PathVariable @Positive(message = "id должен быть > 0") int id) {
        return service.getById(id);
    }
}
