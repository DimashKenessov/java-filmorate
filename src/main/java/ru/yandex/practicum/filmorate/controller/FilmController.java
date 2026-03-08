package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public List<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);

        if (film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn("Ошибка валидации: дата релиза {} раньше 28 декабря 1895 года", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен с id: {}", film.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(film);
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);

        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id {} не найден", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        if (film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn("Ошибка валидации: дата релиза {} раньше 28 декабря 1895 года", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        films.put(film.getId(), film);
        log.info("Фильм с id {} успешно обновлён", film.getId());
        return ResponseEntity.ok(film);
    }
}