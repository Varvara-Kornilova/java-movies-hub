package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MoviesStore {
    private final Map<Long, Movie> movies = new ConcurrentHashMap<>();
    private final AtomicLong currentId = new AtomicLong(1);

    public List<Movie> getAll() {
        return new ArrayList<>(movies.values());
    }

    public Movie add(Movie movie) {
        long id = currentId.getAndIncrement();
        movie.setId(id);
        movies.put(id, movie);
        return movie;
    }

    public Movie findById(long id) {
        return movies.get(id);
    }

    public boolean delete(long id) {
        return movies.remove(id) != null;
    }

    public List<Movie> findByYear(int year) {
        List<Movie> result = new ArrayList<>();
        for (Movie movie : movies.values()) {
            if (movie.getYear() == year) {
                result.add(movie);
            }
        }
        return result;
    }

    public void clear() {
        movies.clear();
        currentId.set(1);
    }
}