package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void testCreateAndFindUser() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userStorage.create(user);
        assertThat(created.getId()).isPositive();

        User found = userStorage.findById(created.getId());
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("test@mail.com");
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("update@mail.com");
        user.setLogin("updatelogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        created.setName("New Name");
        User updated = userStorage.update(created);
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    void testFindAll() {
        List<User> users = userStorage.findAll();
        assertThat(users).isNotNull();
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("delete@mail.com");
        user.setLogin("deletelogin");
        user.setName("Delete Me");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        User created = userStorage.create(user);

        userStorage.delete(created.getId());
        assertThrows(RuntimeException.class, () -> userStorage.findById(created.getId()));
    }
}
