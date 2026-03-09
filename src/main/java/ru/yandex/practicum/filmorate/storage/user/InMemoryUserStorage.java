package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(int id) {
        User user = users.get(id);
        if (user == null) {
            log.warn("User with id {} not found", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("User created with id: {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("User with id {} not found for update", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("User updated with id: {}", user.getId());
        return user;
    }

    @Override
    public void delete(int id) {
        users.remove(id);
        log.info("User deleted with id: {}", id);
    }
}
