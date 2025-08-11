package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.add(user);
    }

    public User update(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + id + " не найден"));
    }

    public void addFriend(int id, int friendId) {
        if (id == friendId) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        getById(id);
        getById(friendId);
        friends.computeIfAbsent(id, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(id);
    }

    public void removeFriend(int id, int friendId) {
        getById(id);
        getById(friendId);
        friends.computeIfAbsent(id, k -> new HashSet<>()).remove(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).remove(id);
    }

    public List<User> getFriends(int id) {
        getById(id);
        return friends.getOrDefault(id, Collections.emptySet()).stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int id, int otherId) {
        getById(id);
        getById(otherId);
        Set<Integer> a = new HashSet<>(friends.getOrDefault(id, Collections.emptySet()));
        Set<Integer> b = new HashSet<>(friends.getOrDefault(otherId, Collections.emptySet()));
        a.retainAll(b);
        return a.stream().map(this::getById).collect(Collectors.toList());
    }

    public boolean exists(int id) {
        return userStorage.getById(id).isPresent();
    }
}
