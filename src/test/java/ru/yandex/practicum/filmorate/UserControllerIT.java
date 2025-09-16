package ru.yandex.practicum.filmorate.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private Map<String, Object> newUserDto() {
        int i = SEQ.getAndIncrement();
        Map<String, Object> u = new HashMap<>();
        u.put("id", 0);
        u.put("email", "u" + i + "@ex.com");
        u.put("login", "u" + i);
        u.put("name", "Name of u" + i);
        u.put("birthday", "1999-01-01");
        return u;
    }

    private int createUserAndReturnId(Map<String, Object> dto) throws Exception {
        String json = om.writeValueAsString(dto);
        String body = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    @DisplayName("create -> get -> update -> list : OK")
    void create_get_update_list_ok() throws Exception {
        Map<String, Object> dto = newUserDto();
        int id = createUserAndReturnId(dto);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==%s)]", id).isArray())
                .andExpect(jsonPath("$[?(@.id==%s)]", id).value(hasSize(1)));

        Map<String, Object> upd = new HashMap<>(dto);
        upd.put("id", id);
        upd.put("name", "Updated Name");
        mvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("friends: add -> list -> common -> remove : OK")
    void friends_add_list_common_remove_ok() throws Exception {
        int u1 = createUserAndReturnId(newUserDto());
        int u2 = createUserAndReturnId(newUserDto());
        int u3 = createUserAndReturnId(newUserDto());

        mvc.perform(put("/users/{id}/friends/{fid}", u1, u2)).andExpect(status().isOk());
        mvc.perform(put("/users/{id}/friends/{fid}", u1, u3)).andExpect(status().isOk());

        mvc.perform(get("/users/{id}/friends", u1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(containsInAnyOrder(u2, u3)));

        // подтверждаем
        mvc.perform(put("/users/{id}/friends/{fid}", u2, u1)).andExpect(status().isOk());
        mvc.perform(put("/users/{id}/friends/{fid}", u3, u1)).andExpect(status().isOk());

        mvc.perform(get("/users/{id}/friends/common/{other}", u2, u3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(u1));

        mvc.perform(delete("/users/{id}/friends/{fid}", u1, u2))
                .andExpect(status().isOk());

        mvc.perform(get("/users/{id}/friends", u1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(Matchers.contains(u3)))
                .andExpect(jsonPath("$[*].id").value(Matchers.not(Matchers.hasItem(u2))));
    }

    @Test
    @DisplayName("валидации и 404: некорректный email и not found")
    void validations_and_not_found() throws Exception {
        Map<String, Object> bad = new HashMap<>();
        bad.put("id", 0);
        bad.put("email", "bad-email");
        bad.put("login", "loginX");
        bad.put("name", "Any");
        bad.put("birthday", LocalDate.of(1990, 1, 1).toString());

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/users/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
