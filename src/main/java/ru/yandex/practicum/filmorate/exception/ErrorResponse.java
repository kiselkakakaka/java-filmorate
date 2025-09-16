package ru.yandex.practicum.filmorate.exception;

import com.fasterxml.jackson.annotation.JsonProperty;


public record ErrorResponse(@JsonProperty("error") String error) { }

