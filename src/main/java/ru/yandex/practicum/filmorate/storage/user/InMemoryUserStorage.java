package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.InvalidIdException;
import ru.yandex.practicum.filmorate.exceptions.userExceptions.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validators.UserValidator;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    private final UserValidator userValidator;
    private int id;

    @Autowired
    public InMemoryUserStorage(UserValidator userValidator) {
        this.userValidator = userValidator;
    }

    public Collection<User> findAllUsers() {
        log.debug("Получен запрос GET /users. Текущее количество пользователей: {}", users.size());
        return users.values();
    }

    public Optional<User> findUserById(int userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public User createUser(@Valid User user) {
        log.debug("Получен запрос POST /users.");
        userValidator.validator(user);
        user.setId(incrementIdCounter());
        int id = user.getId();
        String name = user.getName();
        if (users.containsKey(id)) {
            decrementIdCounter();
            throw new UserAlreadyExistException("Пользователь с электронной почтой " +
                    id + " уже зарегистрирован.");
        }
        if (name == null || name.isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(id, user);
        return user;
    }

    public User updateUser(@Valid User user) {
        log.debug("Получен запрос PUT /users.");
        userValidator.validator(user);
        String name = user.getName();
        int id = user.getId();
        if (name == null || name.isBlank()) {
            user.setName(user.getLogin());
        }
        if (users.containsKey(id)) {
            users.put(id, user);
            return user;
        } else {
            throw new InvalidIdException("Не удалось обновить пользователя. Нет пользователя с id: " + id);
        }
    }

    private int incrementIdCounter() {
        return ++id;
    }

    private int decrementIdCounter() {
        return --id;
    }
}
