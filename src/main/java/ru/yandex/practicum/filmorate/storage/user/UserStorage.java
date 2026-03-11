package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;

public interface UserStorage {
    List<User> findAll();
    User findById(int id);
    User create(User user);
    User update(User user);
    void delete(int id);


    void addFriend(int userId, int friendId);
    void removeFriend(int userId, int friendId);
    List<User> getFriends(int userId);
    List<User> getCommonFriends(int userId, int otherId);
}
