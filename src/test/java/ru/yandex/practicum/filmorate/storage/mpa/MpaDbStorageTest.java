package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTest {

    private final MpaDbStorage mpaDbStorage;
    @Test
    @Sql(scripts = {"file:src/main/resources/setupForTest.sql"})
    public void testFindAllMpa() {
        Collection<Mpa> mpas = mpaDbStorage.findAllMpa();
        assertThat(mpas.size() == 5);
    }

    @Test
    @Sql(scripts = {"file:src/main/resources/setupForTest.sql"})
    public void testFindMpaById() {
        Optional<Mpa> mpaOptional = mpaDbStorage.findMpaById(3);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 3));
        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("name", "PG-13"));
    }
}
