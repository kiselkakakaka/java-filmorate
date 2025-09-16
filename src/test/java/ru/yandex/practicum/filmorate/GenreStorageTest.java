package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.storage.genre.db.DbGenreStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({DbGenreStorage.class})
class GenreStorageTest {

    @Autowired
    private DbGenreStorage storage;

    @Test
    void findById_returnsComedy() {
        var g = storage.findById(1);
        assertThat(g).isPresent();
        assertThat(g.get().getName()).isEqualTo("Комедия");
    }
}

