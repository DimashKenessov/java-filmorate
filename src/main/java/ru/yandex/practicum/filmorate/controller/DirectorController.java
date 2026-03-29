package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorDao directorDao;

    @GetMapping
    public List<Director> findAll() {
        log.info("GET /directors");
        return directorDao.findAll();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable int id) {
        log.info("GET /directors/{}", id);
        return directorDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id " + id + " не найден"));
    }

    @PostMapping
    public ResponseEntity<Director> create(@Valid @RequestBody Director director) {
        log.info("POST /directors: {}", director);
        Director created = directorDao.create(director);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<Director> update(@Valid @RequestBody Director director) {
        log.info("PUT /directors: {}", director);
        Director updated = directorDao.update(director);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        log.info("DELETE /directors/{}", id);
        directorDao.delete(id);
        return ResponseEntity.noContent().build();
    }
}
