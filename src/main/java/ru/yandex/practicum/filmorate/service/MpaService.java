package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.db.DbMpaStorage;

import java.util.List;

@Service
public class MpaService {

    private final DbMpaStorage storage;

    public MpaService(DbMpaStorage storage) {
        this.storage = storage;
    }

    public List<MpaRating> getAll() {
        return storage.findAll();
    }

    public MpaRating getById(int id) {
        return storage.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA с ID " + id + " не найден"));
    }
}
