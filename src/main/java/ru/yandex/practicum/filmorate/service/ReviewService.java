package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedService feedService;

    public Review create(Review review) {
        validateReview(review);
        checkUser(review.getUserId());
        checkFilm(review.getFilmId());

        log.info("Создание отзыва пользователем {} на фильм {}", review.getUserId(), review.getFilmId());

        Review createdReview = reviewStorage.create(review);

        feedService.addEvent(
                createdReview.getUserId(),
                EventType.REVIEW,
                Operation.ADD,
                createdReview.getReviewId()
        );

        return createdReview;
    }

    public Review update(Review review) {
        if (review.getReviewId() == null) {
            throw new ValidationException("Id отзыва должен быть указан");
        }

        validateReview(review);
        checkUser(review.getUserId());
        checkFilm(review.getFilmId());
        reviewStorage.findById(review.getReviewId());

        log.info("Обновление отзыва с id {}", review.getReviewId());

        Review updatedReview = reviewStorage.update(review);

        feedService.addEvent(
                updatedReview.getUserId(),
                EventType.REVIEW,
                Operation.UPDATE,
                updatedReview.getReviewId()
        );

        return updatedReview;
    }

    public void delete(int reviewId) {
        Review review = reviewStorage.findById(reviewId);

        reviewStorage.findById(reviewId);
        log.info("Удаление отзыва с id {}", reviewId);
        reviewStorage.delete(reviewId);

        feedService.addEvent(
                review.getUserId(),
                EventType.REVIEW,
                Operation.REMOVE,
                reviewId
        );
    }

    public Review getById(int reviewId) {
        log.info("Получение отзыва с id {}", reviewId);
        return reviewStorage.findById(reviewId);
    }

    public Collection<Review> getReviews(Integer filmId, int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть больше 0");
        }

        if (filmId != null) {
            checkFilm(filmId);
            log.info("Получение {} отзывов для фильма {}", count, filmId);
            return reviewStorage.findAllByFilmId(filmId, count);
        }

        log.info("Получение {} отзывов по всем фильмам", count);
        return reviewStorage.findAll(count);
    }

    public void addLike(int reviewId, int userId) {
        reviewStorage.findById(reviewId);
        checkUser(userId);

        log.info("Пользователь {} поставил лайк отзыву {}", userId, reviewId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        reviewStorage.findById(reviewId);
        checkUser(userId);

        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, reviewId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(int reviewId, int userId) {
        reviewStorage.findById(reviewId);
        checkUser(userId);

        log.info("Пользователь {} удалил лайк у отзыва {}", userId, reviewId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(int reviewId, int userId) {
        reviewStorage.findById(reviewId);
        checkUser(userId);

        log.info("Пользователь {} удалил дизлайк у отзыва {}", userId, reviewId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Текст отзыва не должен быть пустым");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("Поле isPositive должно быть указано");
        }
        if (review.getUserId() == null) {
            throw new ValidationException("Id пользователя должен быть указан");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("Id фильма должен быть указан");
        }
    }

    private void checkUser(Integer userId) {
        if (userId == null) {
            throw new ValidationException("Id пользователя должен быть указан");
        }

        userStorage.findById(userId);
    }

    private void checkFilm(Integer filmId) {
        if (filmId == null) {
            throw new ValidationException("Id фильма должен быть указан");
        }

        filmStorage.findById(filmId);
    }
}

