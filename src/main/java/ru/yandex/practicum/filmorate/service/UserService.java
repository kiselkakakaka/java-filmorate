package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

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
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    // --- Друзья (примитивная реализация через Set<Integer> в рантайме) ---
    private final java.util.Map<Integer, Set<Integer>> friends = new java.util.HashMap<>();

    public void addFriend(int id, int friendId) {
        ensureExists(id);
        ensureExists(friendId);
        friends.computeIfAbsent(id, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(id);
    }

    public void removeFriend(int id, int friendId) {
        ensureExists(id);
        ensureExists(friendId);
        friends.computeIfAbsent(id, k -> new HashSet<>()).remove(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).remove(id);
    }

    public List<User> getFriends(int id) {
        ensureExists(id);
        Set<Integer> ids = friends.getOrDefault(id, Set.of());
        List<User> result = new ArrayList<>();
        for (Integer fid : ids) {
            userStorage.getById(fid).ifPresent(result::add);
        }
        return result;
    }

    public List<User> getCommonFriends(int id, int otherId) {
        ensureExists(id);
        ensureExists(otherId);
        Set<Integer> a = new HashSet<>(friends.getOrDefault(id, Set.of()));
        a.retainAll(friends.getOrDefault(otherId, Set.of()));
        List<User> result = new ArrayList<>();
        for (Integer fid : a) {
            userStorage.getById(fid).ifPresent(result::add);
        }
        return result;
    }

    public void deleteById(int id) {
        ensureExists(id);
        // удалим связи дружбы
        Set<Integer> fs = new HashSet<>(friends.getOrDefault(id, Set.of()));
        for (Integer f : fs) {
            friends.getOrDefault(f, Set.of()).remove(id);
        }
        friends.remove(id);
        userStorage.deleteById(id);
    }

    private void ensureExists(int id) {
        if (userStorage.getById(id).isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }
}
