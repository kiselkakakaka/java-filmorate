package ru.yandex.practicum.filmorate.storage.common;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MpaRowMapper implements RowMapper<MpaRating> {
    @Override
    public MpaRating mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new MpaRating(
                rs.getInt("id"),
                rs.getString("name")
        );
    }
}

