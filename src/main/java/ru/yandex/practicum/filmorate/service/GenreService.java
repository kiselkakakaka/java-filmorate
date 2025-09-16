package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.db.DbGenreStorage;

import java.util.List;

@Service
public class GenreService {
    private final DbGenreStorage storage;

    public GenreService(DbGenreStorage storage) {
        this.storage = storage;
    }

    public List<Genre> getAll() {
        return storage.findAll();
    }

    public Genre getById(int id) {
        return storage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр не найден: " + id));
    }
}
