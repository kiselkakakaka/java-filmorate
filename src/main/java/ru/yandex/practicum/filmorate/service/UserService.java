package ru.yandex.practicum.filmorate.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Service
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public UserService(UserStorage userStorage, FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public User create(User user) {
        return add(user);
    }

    public User update(User user) {
        ensureExists(user.getId());
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        return userStorage.getById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с ID " + id + " не найден")
        );
    }

    public void deleteById(int id) {
        ensureExists(id);
        userStorage.deleteById(id);
    }

    public User add(User user) {
        return userStorage.add(user);
    }

    public void addFriend(int id, int friendId) {
        ensureExists(id);
        ensureExists(friendId);
        if (id == friendId) {
            throw new IllegalArgumentException("Нельзя добавить в друзья самого себя");
        }
        friendshipStorage.addFriend(id, friendId);
    }

    public void removeFriend(int id, int friendId) {
        ensureExists(id);
        ensureExists(friendId);
        friendshipStorage.removeFriend(id, friendId);
    }

    public List<User> getFriends(int id) {
        ensureExists(id);
        return friendshipStorage.getFriends(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        ensureExists(id);
        ensureExists(otherId);
        return friendshipStorage.getCommonFriends(id, otherId);
    }

    private void ensureExists(int id) {
        Optional<User> u = userStorage.getById(id);
        if (u.isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }
}
