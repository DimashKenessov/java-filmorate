package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.model.Film;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldNotValidateWhenNameIsBlank() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    public void shouldNotValidateWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("a".repeat(201)); // 201 chars
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    public void shouldNotValidateWhenDurationNotPositive() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(-5);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("duration")));
    }

    @Test
    public void shouldValidateWhenAllFieldsCorrect() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }
}
