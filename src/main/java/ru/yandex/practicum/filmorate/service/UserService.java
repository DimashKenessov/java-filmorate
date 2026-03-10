package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public User findById(int id) {
        return userStorage.findById(id);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void addFriend(int userId, int friendId) {
        userStorage.findById(userId);
        userStorage.findById(friendId);

        String checkSql = "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?";
        List<String> statuses = jdbcTemplate.query(checkSql,
                (rs, row) -> rs.getString("status"), friendId, userId);

        if (!statuses.isEmpty() && "unconfirmed".equals(statuses.get(0))) {
            jdbcTemplate.update("UPDATE friendships SET status = 'confirmed' WHERE user_id = ? AND friend_id = ?",
                    friendId, userId);
            jdbcTemplate.update("MERGE INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'confirmed')",
                    userId, friendId);
        } else {
            jdbcTemplate.update("MERGE INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'unconfirmed')",
                    userId, friendId);
        }
        log.info("Friend request processed between {} and {}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(sql, userId, friendId, friendId, userId);
        log.info("Friendship removed between {} and {}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? AND f.status = 'confirmed'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id AND f1.user_id = ? AND f1.status = 'confirmed' " +
                "JOIN friendships f2 ON u.id = f2.friend_id AND f2.user_id = ? AND f2.status = 'confirmed'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId, otherId);
    }
}
