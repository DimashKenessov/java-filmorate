package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface FeedStorage {
    Event addEvent(Event event);

    List<Event> getUserFeed(int userId);
}