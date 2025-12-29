package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.util.JsonUtil;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MovieByIdHandler extends BaseHttpHandler {

    private final MoviesStore store;

    public MovieByIdHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath(); // например, "/movies/123"

        if (!method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("DELETE")) {
            ex.sendResponseHeaders(405, -1);
            ex.close();
            return;
        }

        String idStr = path.substring("/movies/".length());
        long id;

        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            sendJson(ex, 400, JsonUtil.toJson(new ErrorResponse("Некорректный ID")));
            return;
        }

        if (method.equalsIgnoreCase("GET")) {
            handleGet(ex, id);
        } else if (method.equalsIgnoreCase("DELETE")) {
            handleDelete(ex, id);
        }
    }

    private void handleGet(HttpExchange ex, long id) throws IOException {
        Movie movie = store.findById(id);

        if (movie == null) {
            sendJson(ex, 404, JsonUtil.toJson(new ErrorResponse("Фильм не найден")));
            return;
        }

        sendJson(ex, 200, JsonUtil.toJson(movie));
    }

    private void handleDelete(HttpExchange ex, long id) throws IOException {
        boolean deleted = store.delete(id);

        if (!deleted) {
            sendJson(ex, 404, JsonUtil.toJson(new ErrorResponse("Фильм не найден")));
            return;
        }

        sendNoContent(ex);
    }
}
