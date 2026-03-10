package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaDao mpaDao;

    @GetMapping
    public List<Mpa> findAll() {
        log.info("GET /mpa");
        return mpaDao.findAll();
    }

    @GetMapping("/{id}")
    public Mpa findById(@PathVariable int id) {
        log.info("GET /mpa/{}", id);
        return mpaDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + id + " не найден"));
    }
}