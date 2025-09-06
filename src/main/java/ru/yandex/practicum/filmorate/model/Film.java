package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class Film {
    private int id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    private MpaRating mpa;

    private Set<Genre> genres;
}
