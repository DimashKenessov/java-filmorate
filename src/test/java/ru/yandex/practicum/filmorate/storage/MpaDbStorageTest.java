package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@Sql(statements = {
        "DELETE FROM mpa;",
        "INSERT INTO mpa (id, name) VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17');"
})
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    void findAll_ShouldReturnAllMpa() {
        List<Mpa> mpas = mpaStorage.findAll();
        assertThat(mpas).hasSize(5);
        assertThat(mpas.get(0).getName()).isEqualTo("G");
        assertThat(mpas.get(4).getName()).isEqualTo("NC-17");
    }

    @Test
    void findById_WithValidId_ShouldReturnMpa() {
        Optional<Mpa> mpa = mpaStorage.findById(1);
        assertThat(mpa).isPresent();
        assertThat(mpa.get().getName()).isEqualTo("G");
    }

    @Test
    void findById_WithInvalidId_ShouldReturnEmpty() {
        Optional<Mpa> mpa = mpaStorage.findById(999);
        assertThat(mpa).isEmpty();
    }
}