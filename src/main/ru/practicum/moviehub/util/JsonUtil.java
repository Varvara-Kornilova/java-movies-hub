package ru.practicum.moviehub.util;

import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;

import java.util.List;
import java.util.stream.Collectors;

public class JsonUtil {

    public static String toJson(List<Movie> movies) {
        if (movies == null || movies.isEmpty()) {
            return "[]";
        }

        String items = movies.stream()
                .map(JsonUtil::toJson)
                .collect(Collectors.joining(","));
        return "[" + items + "]";
    }

    public static String toJson(Movie movie) {
        if (movie == null) {
            return "null";
        }

        return String.format(
                "{\"id\":%d,\"title\":\"%s\",\"year\":%d}",
                movie.getId(),
                escape(movie.getTitle()),
                movie.getYear()
        );
    }

    public static String toJson(ErrorResponse error) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"error\":\"").append(escape(error.getError())).append("\"");

        if (error.getDetails() != null) {
            String details = error.getDetails().stream()
                    .map(d -> "\"" + escape(d) + "\"")
                    .collect(Collectors.joining(","));
            sb.append(",\"details\":[").append(details).append("]");
        }

        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }

        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
