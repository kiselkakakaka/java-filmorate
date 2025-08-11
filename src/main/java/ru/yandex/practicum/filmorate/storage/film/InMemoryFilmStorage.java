package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final ConcurrentHashMap<Integer, Film> films = new ConcurrentHashMap<>();
    private final AtomicInteger idSeq = new AtomicInteger(0);

    @Override
    public Film add(Film film) {
        int id = idSeq.incrementAndGet();
        film.setId(id);
        films.put(id, film);
        return film;
    }

    @Override
    public Film update(Film film) {
        int id = film.getId();
        if (!films.containsKey(id)) {
            throw new NoSuchElementException("Фильм с ID " + id + " не найден");
        }
        films.put(id, film);
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }
}


