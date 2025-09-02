package ru.yandex.practicum.filmorate.storage.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;

public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film f = new Film();
        f.setId(rs.getInt("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        LocalDate rd = rs.getObject("release_date", LocalDate.class);
        f.setReleaseDate(rd);
        f.setDuration(rs.getInt("duration_min"));
        return f;
    }
}

