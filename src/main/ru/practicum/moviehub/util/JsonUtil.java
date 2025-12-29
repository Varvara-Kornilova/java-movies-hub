package ru.practicum.moviehub.util;

import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class JsonUtil {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static String toJson(List<Movie> movies) {
        return GSON.toJson(movies);
    }

    public static String toJson(Movie movie) {
        return GSON.toJson(movie);
    }

    public static String toJson(ErrorResponse error) {
        return GSON.toJson(error);
    }
}
