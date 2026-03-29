package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void delete(int reviewId);

    Review findById(int reviewId);

    Collection<Review> findAll(int count);

    Collection<Review> findAllByFilmId(int filmId, int count);

    void addLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void removeLike(int reviewId, int userId);

    void removeDislike(int reviewId, int userId);
}