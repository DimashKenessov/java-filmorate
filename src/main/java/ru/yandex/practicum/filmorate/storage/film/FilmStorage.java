package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;

public interface FilmStorage {
    List<Film> findAll();
    Film findById(int id);
    Film create(Film film);
    Film update(Film film);
    void delete(int id);
    void addLike(int filmId, int userId);
    void removeLike(int filmId, int userId);
    List<Film> getPopular(int count);
}
