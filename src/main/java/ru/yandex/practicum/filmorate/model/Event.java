package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Event {
    private Long timestamp;
    private Integer userId;
    private EventType eventType;
    private Operation operation;
    private Integer eventId;
    private Integer entityId;
}