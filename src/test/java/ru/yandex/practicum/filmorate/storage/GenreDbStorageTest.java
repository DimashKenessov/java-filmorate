package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@Sql(statements = {
        "DELETE FROM genres;",
        "INSERT INTO genres (id, name) VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм'), (4, 'Триллер'), (5, 'Документальный'), (6, 'Боевик');"
})
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    void findAll_ShouldReturnAllGenres() {
        List<Genre> genres = genreStorage.findAll();
        assertThat(genres).hasSize(6);
        assertThat(genres.get(0).getName()).isEqualTo("Комедия");
    }

    @Test
    void findById_ValidId_ReturnsGenre() {
        Optional<Genre> genre = genreStorage.findById(1);
        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }

    @Test
    void findById_InvalidId_ReturnsEmpty() {
        Optional<Genre> genre = genreStorage.findById(999);
        assertThat(genre).isEmpty();
    }
}
