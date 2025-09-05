package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.db.DbMpaStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(DbMpaStorage.class)
class MpaStorageTest {

    @Autowired
    private DbMpaStorage storage;

    @Test
    void findAll_returnsNonEmptyOrderedList() {
        List<MpaRating> all = storage.findAll();
        assertThat(all).isNotNull();
        assertThat(all).isNotEmpty();
        // убеждаемся, что id идут по возрастанию (как в ORDER BY id)
        assertThat(all).isSortedAccordingTo((a, b) -> Integer.compare(a.getId(), b.getId()));
    }

    @Test
    void findById_returnsGForId1() {
        Optional<MpaRating> mpa = storage.findById(1);
        assertThat(mpa).isPresent();
        assertThat(mpa.get().getName()).isEqualTo("G");
    }
}

