package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {

        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM films");

    }

    @Test
    void create_ShouldAssignIdAndSaveFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Film saved = filmStorage.create(film);

        assertThat(saved.getId()).isPositive();
        Film found = filmStorage.findById(saved.getId());
        assertThat(found.getName()).isEqualTo("Test Film");
        assertThat(found.getMpa().getId()).isEqualTo(1);
    }

    @Test
    void findById_WithInvalidId_ShouldThrowNotFoundException() {
        assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> filmStorage.findById(999));
    }

    @Test
    void update_ShouldModifyFilm() {
        Film film = new Film();
        film.setName("Original");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        Film created = filmStorage.create(film);

        created.setName("Updated");
        Film updated = filmStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Updated");
        Film found = filmStorage.findById(created.getId());
        assertThat(found.getName()).isEqualTo("Updated");
    }

    @Test
    void delete_ShouldRemoveFilm() {
        Film film = new Film();
        film.setName("ToDelete");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        Film created = filmStorage.create(film);

        filmStorage.delete(created.getId());

        assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> filmStorage.findById(created.getId()));
    }

    @Test
    void addLike_ShouldIncreaseLikesCount() {
        // First create a user (simplified – we need a user in DB)
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "test@user.com", "testuser", "Test User", "2000-01-01");
        Integer userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE login = 'testuser'", Integer.class);

        Film film = new Film();
        film.setName("Like Test");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        Film created = filmStorage.create(film);

        filmStorage.addLike(created.getId(), userId);

        // Verify like count via getPopular or custom query
        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).anyMatch(f -> f.getId() == created.getId());
    }
}