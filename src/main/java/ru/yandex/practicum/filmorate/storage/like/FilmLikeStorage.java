package ru.yandex.practicum.filmorate.storage.like;

import java.util.List;
import ru.yandex.practicum.filmorate.model.Film;

public interface FilmLikeStorage {
    void addLike(int filmId, int userId);
    void removeLike(int filmId, int userId);
    List<Film> findPopular(int limit);
}
