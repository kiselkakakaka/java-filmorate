package ru.yandex.practicum.filmorate.model;

import java.util.Arrays;
import java.util.Optional;

public enum Genre {
    COMEDY(1, "Комедия"),
    DRAMA(2, "Драма"),
    CARTOON(3, "Мультфильм"),
    THRILLER(4, "Триллер"),
    DOCUMENTARY(5, "Документальный"),
    ACTION(6, "Боевик");

    private final int id;
    private final String name;

    Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public static Optional<Genre> fromId(int id) {
        return Arrays.stream(values()).filter(g -> g.id == id).findFirst();
    }
}


