package ru.yandex.practicum.filmorate.model;

import java.util.Arrays;
import java.util.Optional;

public enum MpaRating {
    G(1, "G"),
    PG(2, "PG"),
    PG_13(3, "PG-13"),
    R(4, "R"),
    NC_17(5, "NC-17");

    private final int id;
    private final String name;

    MpaRating(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public static Optional<MpaRating> fromId(int id) {
        return Arrays.stream(values()).filter(m -> m.id == id).findFirst();
    }
}


