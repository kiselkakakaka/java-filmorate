package ru.yandex.practicum.filmorate.storage.friendship.db;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.common.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;

@Repository
public class DbFriendshipStorage implements FriendshipStorage {

    private final JdbcTemplate jdbc;

    public DbFriendshipStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sql = "INSERT INTO friendships (requester_id, addressee_id, status) " +
                "VALUES (?, ?, 'CONFIRMED') " +
                "ON CONFLICT (requester_id, addressee_id) DO NOTHING";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbc.update("DELETE FROM friendships WHERE requester_id = ? AND addressee_id = ?", userId, friendId);
        jdbc.update("DELETE FROM friendships WHERE requester_id = ? AND addressee_id = ?", friendId, userId);
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql =
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM friendships fr " +
                        "JOIN users u ON (u.id = CASE WHEN fr.requester_id = ? THEN fr.addressee_id ELSE fr.requester_id END) " +
                        "WHERE fr.status = 'CONFIRMED' AND (? IN (fr.requester_id, fr.addressee_id)) " +
                        "ORDER BY u.id";
        return jdbc.query(sql, new UserRowMapper(), userId, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql =
                "WITH u1 AS ( " +
                        "  SELECT CASE WHEN requester_id = ? THEN addressee_id ELSE requester_id END AS fid " +
                        "  FROM friendships WHERE status='CONFIRMED' AND (? IN (requester_id, addressee_id)) " +
                        "), u2 AS ( " +
                        "  SELECT CASE WHEN requester_id = ? THEN addressee_id ELSE requester_id END AS fid " +
                        "  FROM friendships WHERE status='CONFIRMED' AND (? IN (requester_id, addressee_id)) " +
                        ") " +
                        "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM u1 JOIN u2 USING(fid) " +
                        "JOIN users u ON u.id = u1.fid " +
                        "ORDER BY u.id";
        return jdbc.query(sql, new UserRowMapper(), userId, userId, otherId, otherId);
    }
}

