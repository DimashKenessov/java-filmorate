package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;
import java.util.List;
import java.util.Optional;

public interface DirectorDao {
    List<Director> findAll();
    Optional<Director> findById(int id);
    Director create(Director director);
    Director update(Director director);
    void delete(int id);
    List<Director> findDirectorsByFilmId(int filmId);
    void addDirectorsToFilm(int filmId, List<Integer> directorIds);
    void removeDirectorsFromFilm(int filmId);
}
