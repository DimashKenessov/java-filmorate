package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDao mpaDao;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, MpaDao mpaDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaDao = mpaDao;
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, getFilmMapper());
        films.forEach(this::loadGenres);
        return films;
    }

    @Override
    public Film findById(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, getFilmMapper(), id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        Film film = films.get(0);
        loadGenres(film);
        return film;
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            insertGenres(film);
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        findById(film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            insertGenres(film);
        }
        return film;
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "MERGE INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.* FROM films f LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id ORDER BY COUNT(l.user_id) DESC LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, getFilmMapper(), count);
        films.forEach(this::loadGenres);
        return films;
    }

    @Override
    public List<Film> findFilmsBySearchQuery(String query, List<String> by) {
        String preparedQuery = "%" + query + "%";
        String sql = "SELECT f.* FROM films f "
                + "LEFT JOIN likes l ON f.id = l.film_id "
                + "WHERE name LIKE ? "
                + "GROUP BY f.id "
                + "ORDER BY COUNT(l.user_id) DESC LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, getFilmMapper(),preparedQuery, 10);
        films.forEach(this::loadGenres);
    public List<Film> getRecommendations(int userId) {
        String sqlLikes = "SELECT user_id, film_id FROM likes";
        Map<Integer, Map<Integer, Double>> userLikes = new  HashMap<>();

        jdbcTemplate.query(sqlLikes, rs -> {
            int uId = rs.getInt("user_id");
            int fId = rs.getInt("film_id");
            userLikes.computeIfAbsent(uId, k -> new HashMap<>()).put(fId, 1.0);
        });

        Map<Integer, Double> targetUserLikes = userLikes.getOrDefault(userId, Collections.emptyMap());

        if (targetUserLikes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, Map<Integer, Double>> diff = new  HashMap<>();
        Map<Integer, Map<Integer, Integer>> freq = new  HashMap<>();

        for (Map<Integer, Double> userRatings : userLikes.values()) {
            for (Map.Entry<Integer, Double> itemI : userRatings.entrySet()) {
                diff.putIfAbsent(itemI.getKey(), new HashMap<>());
                freq.putIfAbsent(itemI.getKey(), new HashMap<>());

                for (Map.Entry<Integer, Double> itemJ : userRatings.entrySet()) {
                    int count = freq.get(itemI.getKey()).getOrDefault(itemJ.getKey(), 0);

                    freq.get(itemI.getKey()).put(itemJ.getKey(), count + 1);

                    double oldDiff = diff.get(itemI.getKey()).getOrDefault(itemJ.getKey(), 0.0);
                    double newDiff = itemI.getValue() - itemJ.getValue();

                    diff.get(itemI.getKey()).put(itemJ.getKey(), oldDiff + newDiff);
                }
            }
        }

        for (Integer itemI : diff.keySet()) {
            for (Integer itemJ : diff.get(itemI).keySet()) {
                double totalDiff = diff.get(itemI).get(itemJ);
                int count =  freq.get(itemI).get(itemJ);
                diff.get(itemI).put(itemJ, totalDiff / count);
            }
        }

        Map<Integer, Double> predictions = new HashMap<>();
        Map<Integer, Integer> weight = new HashMap<>();

        for (Integer itemJ : diff.keySet()) {
            if (targetUserLikes.containsKey(itemJ)) {
                continue;
            }

            double sum = 0.0;
            int totalFreq = 0;

            for (Map.Entry<Integer, Double> targetItem : targetUserLikes.entrySet()) {
                Integer itemI = targetItem.getKey();
                Double ratingI = targetItem.getValue();

                if (freq.containsKey(itemJ) && freq.get(itemJ).containsKey(itemI)) {
                    int count = freq.get(itemJ).get(itemI);
                    sum += (ratingI + diff.get(itemJ).get(itemI)) * count;
                    totalFreq += count;
                }
            }

            if (totalFreq > 0) {
                predictions.put(itemJ, sum / totalFreq);
                weight.put(itemJ, totalFreq);
            }
        }

        List<Integer> recommendedFilmIds = predictions.keySet().stream()
                .sorted((f1, f2) -> {
                    int scoreCompare = Double.compare(predictions.get(f2), predictions.get(f1));
                    if (scoreCompare != 0) return scoreCompare;
                    return Integer.compare(weight.get(f2), weight.get(f1));
                })
                .collect(Collectors.toList());

        if (recommendedFilmIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inSql = String.join(",", Collections.nCopies(recommendedFilmIds.size(), "?"));
        String filmSql = String.format("SELECT * FROM films WHERE id IN (%s)", inSql);
        List<Film> resultFilms = jdbcTemplate.query(filmSql, getFilmMapper(), recommendedFilmIds.toArray());

        resultFilms.forEach(this :: loadGenres);
        Map<Integer, Film> filmMap = resultFilms.stream().collect(Collectors.toMap(Film::getId, f -> f));

        return recommendedFilmIds.stream().map(filmMap :: get).collect(Collectors.toList());
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = "SELECT f.* " +
                "FROM films as f " +
                "JOIN likes as l1 ON f.id = l1.film_id AND l1.user_id = ? " +
                "JOIN likes as l2 ON f.id = l2.film_id AND l2.user_id = ? " +
                "LEFT JOIN likes as l3 ON f.id = l3.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l3.user_id) DESC";

        List<Film> films = jdbcTemplate.query(sql, getFilmMapper(), userId, friendId);
        films.forEach(this :: loadGenres);
        return films;
    }

    private RowMapper<Film> getFilmMapper() {
        return (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            int mpaId = rs.getInt("mpa_id");
            if (mpaId != 0) {
                mpaDao.findById(mpaId).ifPresent(film::setMpa);
            }
            return film;
        };
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre g = new Genre();
            g.setId(rs.getInt("id"));
            g.setName(rs.getString("name"));
            return g;
        }, film.getId());
        film.setGenres(new HashSet<>(genres));
    }

    private void insertGenres(Film film) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }
}