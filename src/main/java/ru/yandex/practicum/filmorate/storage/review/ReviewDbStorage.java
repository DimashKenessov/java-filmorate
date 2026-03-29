package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("reviewDbStorage")
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sql = """
                INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"review_id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            stmt.setInt(5, 0);
            return stmt;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().intValue());
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = """
                UPDATE reviews
                SET content = ?, is_positive = ?
                WHERE review_id = ?
                """;

        int rows = jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        if (rows == 0) {
            throw new NotFoundException("Отзыв с id " + review.getReviewId() + " не найден");
        }

        return findById(review.getReviewId());
    }

    @Override
    public void delete(int reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        int rows = jdbcTemplate.update(sql, reviewId);

        if (rows == 0) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
    }

    @Override
    public Review findById(int reviewId) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";

        List<Review> reviews = jdbcTemplate.query(sql, this::mapRowToReview, reviewId);

        if (reviews.isEmpty()) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }

        return reviews.get(0);
    }

    @Override
    public Collection<Review> findAll(int count) {
        String sql = """
                SELECT *
                FROM reviews
                ORDER BY useful DESC, review_id ASC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, this::mapRowToReview, count);
    }

    @Override
    public Collection<Review> findAllByFilmId(int filmId, int count) {
        String sql = """
                SELECT *
                FROM reviews
                WHERE film_id = ?
                ORDER BY useful DESC, review_id ASC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        Map<String, Object> reaction = getReaction(reviewId, userId);

        if (reaction == null) {
            jdbcTemplate.update(
                    "INSERT INTO review_reactions (review_id, user_id, is_like) VALUES (?, ?, ?)",
                    reviewId, userId, true
            );
            updateUseful(reviewId, 1);
            return;
        }

        boolean isLike = (boolean) reaction.get("is_like");

        if (isLike) {
            return;
        }

        jdbcTemplate.update(
                "UPDATE review_reactions SET is_like = ? WHERE review_id = ? AND user_id = ?",
                true, reviewId, userId
        );
        updateUseful(reviewId, 2);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        Map<String, Object> reaction = getReaction(reviewId, userId);

        if (reaction == null) {
            jdbcTemplate.update(
                    "INSERT INTO review_reactions (review_id, user_id, is_like) VALUES (?, ?, ?)",
                    reviewId, userId, false
            );
            updateUseful(reviewId, -1);
            return;
        }

        boolean isLike = (boolean) reaction.get("is_like");

        if (!isLike) {
            return;
        }

        jdbcTemplate.update(
                "UPDATE review_reactions SET is_like = ? WHERE review_id = ? AND user_id = ?",
                false, reviewId, userId
        );
        updateUseful(reviewId, -2);
    }

    @Override
    public void removeLike(int reviewId, int userId) {
        Map<String, Object> reaction = getReaction(reviewId, userId);

        if (reaction == null) {
            return;
        }

        boolean isLike = (boolean) reaction.get("is_like");

        if (!isLike) {
            return;
        }

        jdbcTemplate.update(
                "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ?",
                reviewId, userId
        );
        updateUseful(reviewId, -1);
    }

    @Override
    public void removeDislike(int reviewId, int userId) {
        Map<String, Object> reaction = getReaction(reviewId, userId);

        if (reaction == null) {
            return;
        }

        boolean isLike = (boolean) reaction.get("is_like");

        if (isLike) {
            return;
        }

        jdbcTemplate.update(
                "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ?",
                reviewId, userId
        );
        updateUseful(reviewId, 1);
    }

    private Map<String, Object> getReaction(int reviewId, int userId) {
        String sql = """
                SELECT review_id, user_id, is_like
                FROM review_reactions
                WHERE review_id = ? AND user_id = ?
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, reviewId, userId);

        if (rows.isEmpty()) {
            return null;
        }

        return rows.get(0);
    }

    private void updateUseful(int reviewId, int delta) {
        String sql = """
                UPDATE reviews
                SET useful = useful + ?
                WHERE review_id = ?
                """;
        jdbcTemplate.update(sql, delta, reviewId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getInt("user_id"));
        review.setFilmId(rs.getInt("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    }
}