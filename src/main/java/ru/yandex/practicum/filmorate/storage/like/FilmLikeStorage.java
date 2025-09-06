package ru.yandex.practicum.filmorate.storage.like;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmLikeStorage {
    void addLike(int filmId, int userId);
    void removeLike(int filmId, int userId);
    List<Film> findPopular(int count);
}
