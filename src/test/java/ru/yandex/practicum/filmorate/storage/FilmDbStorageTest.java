package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Test
    void testCreateAndFindFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film created = filmStorage.create(film);
        assertThat(created.getId()).isPositive();

        Film found = filmStorage.findById(created.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test Film");
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Old Name");
        film.setDescription("Old Desc");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        Film created = filmStorage.create(film);

        created.setName("New Name");
        Film updated = filmStorage.update(created);
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    void testFindAll() {
        List<Film> films = filmStorage.findAll();
        assertThat(films).isNotNull();
    }
}
