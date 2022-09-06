package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.exceptions.filmExceptions.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private FilmController filmController;
    private final static LocalDate TEST_DATE = LocalDate.of(1895, 12, 29);
    private final static long FILM_DURATION = 200;
    private final static long NEGATIVE_FILM_DURATION = -1;
    private final static long ZERO_FILM_DURATION = 0;

    @Test
    void tryToCreateAndUpdateFilmWithCorrectData() throws Exception {
        Film film = new Film(1, "name", RandomString.make(200), TEST_DATE,
                FILM_DURATION);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        film = new Film(1, "name2", RandomString.make(200), TEST_DATE,
                FILM_DURATION);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    void tryToCreateFilmWithEarlyDateBadRequest() throws Exception {
        Film film = new Film(1, "name", RandomString.make(200), TEST_DATE.minusDays(2),
                FILM_DURATION);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ReleaseDateException))
                .andExpect(result -> assertEquals("Доступная дата релиза не может быть раньше 1895-12-28",
                        result.getResolvedException().getMessage()));
    }

    @Test
    void tryToCreateFilmEmptyNameBadRequest() throws Exception {
        Film film = new Film(1, "", RandomString.make(200), TEST_DATE, FILM_DURATION);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    void tryToCreateFilmWithDescriptionLength201BadRequest() throws Exception {
        Film film = new Film(1, "name", RandomString.make(201), TEST_DATE,
                FILM_DURATION);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(result
                        -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }


    @Test
    void tryToCreateFilmWithNegativeDurationZeroBadRequest() throws Exception {
        Film film = new Film(1, "name", RandomString.make(200), TEST_DATE,
                ZERO_FILM_DURATION);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(result
                        -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    void tryToCreateFilmWithNegativeDurationMinus1BadRequest() throws Exception {
        Film film = new Film(1, "name", RandomString.make(200), TEST_DATE,
                NEGATIVE_FILM_DURATION);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(result
                        -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    void tryToUpdateFilmWithEarlyDateBadRequest() throws Exception {
        Film film = new Film(1, "name", RandomString.make(200), TEST_DATE,
                FILM_DURATION);
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        film = new Film(1, "name", RandomString.make(200), TEST_DATE.minusDays(2),
                FILM_DURATION);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ReleaseDateException))
                .andExpect(result -> assertEquals("Доступная дата релиза не может быть раньше 1895-12-28",
                        result.getResolvedException().getMessage()));
    }

    @Test
    void tryToUpdateFilmWithIncorrectIdBadRequest() throws Exception {
        Film film = new Film(1, "name", RandomString.make(200), TEST_DATE,
                FILM_DURATION);
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        film = new Film(2, "name", RandomString.make(200), TEST_DATE,
                FILM_DURATION);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidIdException))
                .andExpect(result -> assertEquals("Не удалось обновить фильм. Нет фильма с id: 2",
                        result.getResolvedException().getMessage()));
    }

    @Test
    void tryTofindAll() throws Exception {
        Film film1 = new Film(1, "name", RandomString.make(200), TEST_DATE,
                FILM_DURATION);
        Film film2 = new Film(2, "name", RandomString.make(200), TEST_DATE,
                FILM_DURATION);
        Film film3 = new Film(3, "name", RandomString.make(200), TEST_DATE,
                FILM_DURATION);
        Mockito.when(filmController.findAll()).thenReturn(Arrays.asList(film1, film2, film3));
        mockMvc.perform(
                        get("/films")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString
                        (Arrays.asList(film1, film2, film3))));
    }


}