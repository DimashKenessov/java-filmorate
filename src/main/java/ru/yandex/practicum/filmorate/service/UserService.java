package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

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
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("User {} and user {} are now friends", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("User {} and user {} are no longer friends", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.findById(userId);
        return user.getFriends().stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = userStorage.findById(userId);
        User other = userStorage.findById(otherId);
        Set<Integer> commonIds = user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .collect(Collectors.toSet());
        return commonIds.stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}
