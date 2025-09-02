package ru.yandex.practicum.filmorate.storage.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.User;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setLogin(rs.getString("login"));
        u.setName(rs.getString("name"));
        LocalDate bd = rs.getObject("birthday", LocalDate.class);
        u.setBirthday(bd);
        return u;
    }
}

