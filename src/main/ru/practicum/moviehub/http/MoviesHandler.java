package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.util.JsonUtil;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {

    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            String query = ex.getRequestURI().getQuery();

            if (query != null && query.startsWith("year=")) {
                handleGetByYear(ex, query.substring(5));
            } else {
                handleGetAll(ex);
            }
        } else if (method.equalsIgnoreCase("POST")) {
            handlePost(ex);
        } else {
            ex.sendResponseHeaders(405, -1);
            ex.close();
        }
    }

    private void handleGetAll(HttpExchange ex) throws IOException {
        sendJson(ex, 200, JsonUtil.toJson(store.getAll()));
    }

    private void handleGetByYear(HttpExchange ex, String yearParam) throws IOException {
        try {
            int year = Integer.parseInt(yearParam);
            sendJson(ex, 200, JsonUtil.toJson(store.findByYear(year)));
        } catch (NumberFormatException e) {
            sendJson(ex, 400, JsonUtil.toJson(new ErrorResponse("Некорректный параметр запроса — 'year'")));
        }
    }

    private static final Gson gson = new Gson();

    private void handlePost(HttpExchange ex) throws IOException {
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");

        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            sendJson(ex, 415, JsonUtil.toJson(new ErrorResponse("Неподдерживаемый тип медиа")));
            return;
        }

        String body = readBody(ex);
        Movie movie;

        try {
            movie = gson.fromJson(body, Movie.class);
        } catch (com.google.gson.JsonSyntaxException e) {
            sendJson(ex, 422, JsonUtil.toJson(new ErrorResponse("Ошибка валидации",
                    List.of("Некорректный JSON"))));
            return;
        } catch (Exception e) {
            sendJson(ex, 422, JsonUtil.toJson(new ErrorResponse("Ошибка валидации",
                    List.of("Не удалось распарсить запрос"))));
            return;
        }

        if (movie == null) {
            sendJson(ex, 422, JsonUtil.toJson(new ErrorResponse("Ошибка валидации",
                    List.of("Пустое тело запроса"))));
            return;
        }

        List<String> errors = validateMovie(movie);

        if (!errors.isEmpty()) {
            sendJson(ex, 422, JsonUtil.toJson(new ErrorResponse("Ошибка валидации", errors)));
            return;
        }

        Movie saved = store.add(movie);
        sendJson(ex, 201, JsonUtil.toJson(saved));
    }

    private Movie parseMovieFromBody(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("Пустое тело запроса");
        }
        try {
            String title = extractField(json, "title");
            int year = Integer.parseInt(extractField(json, "year"));
            return new Movie(0, title, year);
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректный JSON");
        }
    }

    private String extractField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":";
        int start = json.indexOf(pattern);

        if (start == -1) {
            throw new IllegalArgumentException("Отсутствует поле '" + fieldName + "'");
        }

        start += pattern.length();

        while (start < json.length() && json.charAt(start) == ' ') {
            start++;
        }

        if (start >= json.length()) {
            throw new IllegalArgumentException("Неверный формат JSON");
        }

        char firstChar = json.charAt(start);

        if (firstChar == '"') {
            int end = json.indexOf('"', start + 1);

            if (end == -1) {
                throw new IllegalArgumentException("Незакрытая кавычка в поле '" + fieldName + "'");
            }

            return json.substring(start + 1, end);
        } else {
            int end = json.indexOf(',', start);
            int end2 = json.indexOf('}', start);

            if (end == -1) {
                end = end2;
            }
            if (end == -1) {
                end = json.length();
            }

            return json.substring(start, end).trim();
        }
    }

    private List<String> validateMovie(Movie movie) {
        List<String> errors = new ArrayList<>();
        int currentYear = java.time.Year.now().getValue();

        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            errors.add("название не должно быть пустым");
        } else if (movie.getTitle().length() > 100) {
            errors.add("название не должно превышать 100 символов");
        }

        if (movie.getYear() < 1888 || movie.getYear() > currentYear + 1) {
            errors.add("год должен быть между 1888 и " + (currentYear + 1));
        }

        return errors;
    }
}
