package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findById(int id) {
        Film film = films.get(id);
        if (film == null) {
            log.warn("Film with id {} not found", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
    }

    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Film created with id: {}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.warn("Film with id {} not found for update", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("Film updated with id: {}", film.getId());
        return film;
    }

    @Override
    public void delete(int id) {
        films.remove(id);
        log.info("Film deleted with id: {}", id);
    }
}
