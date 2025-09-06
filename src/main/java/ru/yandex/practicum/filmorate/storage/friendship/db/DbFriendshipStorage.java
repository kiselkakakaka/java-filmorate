package ru.yandex.practicum.filmorate.storage.friendship.db;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.common.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;

import java.util.List;

@Repository
@Primary
@RequiredArgsConstructor
public class DbFriendshipStorage implements FriendshipStorage {

    private final JdbcTemplate jdbc;

    @Override
    public void addFriend(int userId, int friendId) {
        // Идемпотентно: H2 поддерживает MERGE ... KEY(...)
        final String sql = """
            MERGE INTO friendships (user_id, friend_id, created_at)
            KEY (user_id, friend_id)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            """;
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbc.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        final String sql = """
            SELECT u.id, u.email, u.login, u.name, u.birthday
              FROM friendships f
              JOIN users u ON u.id = f.friend_id
             WHERE f.user_id = ?
             ORDER BY u.id
            """;
        return jdbc.query(sql, new UserRowMapper(), userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        final String sql = """
            SELECT u.id, u.email, u.login, u.name, u.birthday
              FROM friendships f1
              JOIN friendships f2 ON f2.friend_id = f1.friend_id
              JOIN users u ON u.id = f1.friend_id
             WHERE f1.user_id = ?
               AND f2.user_id = ?
             ORDER BY u.id
            """;
        return jdbc.query(sql, new UserRowMapper(), userId, otherId);
    }
}
