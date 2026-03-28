package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Director {
    private int id;

    @NotBlank(message = "Имя режиссёра не может быть пустым")
    private String name;
}
