package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void create_ShouldAssignIdAndSaveUser() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User saved = userStorage.create(user);

        assertThat(saved.getId()).isPositive();
        User found = userStorage.findById(saved.getId());
        assertThat(found.getEmail()).isEqualTo("test@mail.com");
        assertThat(found.getLogin()).isEqualTo("testlogin");
    }

    @Test
    void findById_WithInvalidId_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> userStorage.findById(999));
    }

    @Test
    void update_ShouldModifyUser() {
        User user = new User();
        user.setEmail("original@mail.com");
        user.setLogin("original");
        user.setName("Original");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        created.setName("Updated");
        created.setEmail("updated@mail.com");
        User updated = userStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Updated");
        User found = userStorage.findById(created.getId());
        assertThat(found.getEmail()).isEqualTo("updated@mail.com");
    }

    @Test
    void delete_ShouldRemoveUser() {
        User user = new User();
        user.setEmail("delete@mail.com");
        user.setLogin("deletelogin");
        user.setName("Delete Me");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        User created = userStorage.create(user);

        userStorage.delete(created.getId());

        assertThrows(NotFoundException.class, () -> userStorage.findById(created.getId()));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {

        List<User> users = userStorage.findAll();
        assertThat(users).isNotEmpty();
    }
}