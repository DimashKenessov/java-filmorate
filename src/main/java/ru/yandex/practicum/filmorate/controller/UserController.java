package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public List<User> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя не задано, используется логин: {}", user.getLogin());
        }

        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с id: {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping
    public ResponseEntity<User> update(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя: {}", user);

        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id {} не найден", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя не задано, используется логин: {}", user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Пользователь с id {} успешно обновлён", user.getId());
        return ResponseEntity.ok(user);
    }
}
