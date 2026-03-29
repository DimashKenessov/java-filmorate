package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorDao {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Director> directorMapper = (rs, rowNum) -> {
        Director director = new Director();
        director.setId(rs.getInt("id"));
        director.setName(rs.getString("name"));
        return director;
    };

    @Override
    public List<Director> findAll() {
        String sql = "SELECT * FROM directors ORDER BY id";
        return jdbcTemplate.query(sql, directorMapper);
    }

    @Override
    public Optional<Director> findById(int id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        List<Director> directors = jdbcTemplate.query(sql, directorMapper, id);
        return directors.stream().findFirst();
    }

    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        if (findById(director.getId()).isEmpty()) {
            throw new NotFoundException("Режиссёр с id " + director.getId() + " не найден");
        }
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, director.getName(), director.getId());
        return director;
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM directors WHERE id = ?", id);
    }

    @Override
    public List<Director> findDirectorsByFilmId(int filmId) {
        String sql = "SELECT d.* FROM directors d " +
                "JOIN film_directors fd ON d.id = fd.director_id " +
                "WHERE fd.film_id = ? ORDER BY d.id";
        return jdbcTemplate.query(sql, directorMapper, filmId);
    }

    @Override
    public void addDirectorsToFilm(int filmId, List<Integer> directorIds) {
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
        for (Integer directorId : directorIds) {
            jdbcTemplate.update(sql, filmId, directorId);
        }
    }

    @Override
    public void removeDirectorsFromFilm(int filmId) {
        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", filmId);
    }
}
