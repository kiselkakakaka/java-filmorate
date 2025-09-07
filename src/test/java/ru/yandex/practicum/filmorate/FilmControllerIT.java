package ru.yandex.practicum.filmorate.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    private Map<String, Object> mpa(int id, String name) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        return m;
    }

    private Map<String, Object> genre(int id, String name) {
        Map<String, Object> g = new HashMap<>();
        g.put("id", id);
        g.put("name", name);
        return g;
    }

    private Map<String, Object> newFilmDto(String name) {
        Map<String, Object> f = new HashMap<>();
        f.put("id", 0);
        f.put("name", name);
        f.put("description", "Space drama");
        f.put("releaseDate", "2014-11-07");
        f.put("duration", 169);
        f.put("mpa", mpa(3, "PG-13"));
        f.put("genres", Collections.singletonList(genre(2, "Драма")));
        return f;
    }

    private Map<String, Object> newUserDto() {
        String unique = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> u = new HashMap<>();
        u.put("id", 0);
        u.put("email", "u_" + unique + "@example.com");
        u.put("login", "u_" + unique);
        u.put("name", "User " + unique.substring(0, 6));
        u.put("birthday", "2000-01-01");
        return u;
    }

    private int createUser() throws Exception {
        String body = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(newUserDto())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private int createFilmAndReturnId(Map<String, Object> dto) throws Exception {
        String json = om.writeValueAsString(dto);
        String body = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    @DisplayName("create -> get : OK")
    void create_and_get_film_ok() throws Exception {
        int id = createFilmAndReturnId(newFilmDto("Interstellar"));

        mvc.perform(get("/films/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Interstellar"));
    }

    @Test
    @DisplayName("update -> getAll : OK")
    void update_and_get_all_ok() throws Exception {
        int id = createFilmAndReturnId(newFilmDto("Interstellar"));

        Map<String, Object> upd = newFilmDto("Interstellar (upd)");
        upd.put("id", id);

        mvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Interstellar (upd)"));

        mvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==%s && @.name=='Interstellar (upd)')]", id).isArray())
                .andExpect(jsonPath("$[?(@.id==%s && @.name=='Interstellar (upd)')]", id).value(hasSize(1)));
    }

    @Test
    @DisplayName("like/unlike и /films/popular : OK")
    void like_unlike_and_popular_ok() throws Exception {
        int f1 = createFilmAndReturnId(newFilmDto("Interstellar"));
        int f2 = createFilmAndReturnId(newFilmDto("Inception"));

        int u1 = createUser();
        int u2 = createUser();
        int u3 = createUser();

        mvc.perform(put("/films/{id}/like/{uid}", f1, u1)).andExpect(status().isOk());
        mvc.perform(put("/films/{id}/like/{uid}", f1, u2)).andExpect(status().isOk());
        mvc.perform(put("/films/{id}/like/{uid}", f2, u3)).andExpect(status().isOk());

        mvc.perform(get("/films/popular").param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].id").value(f1))
                .andExpect(jsonPath("$[1].id").value(f2));

        mvc.perform(delete("/films/{id}/like/{uid}", f1, u1)).andExpect(status().isOk());

        mvc.perform(get("/films/popular").param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("валидации и 404 по фильмам")
    void validations_and_not_found() throws Exception {
        Map<String, Object> bad = new HashMap<>(newFilmDto("Old Movie"));
        bad.put("releaseDate", "1800-01-01");

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/films/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
