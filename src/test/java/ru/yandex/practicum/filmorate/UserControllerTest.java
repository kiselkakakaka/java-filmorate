package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.GlobalExceptionHandler;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean UserService userService;

    @Test
    void shouldUseLoginAsNameIfNameIsBlank() throws Exception {
        User in = new User();
        in.setEmail("mail@test.ru");
        in.setLogin("login123");
        in.setName(""); // пустое имя — контроллер должен подставить логин
        in.setBirthday(LocalDate.of(1990, 1, 1));

                User out = new User();
        out.setId(1);
        out.setEmail(in.getEmail());
        out.setLogin(in.getLogin());
        out.setName(in.getLogin());
        out.setBirthday(in.getBirthday());

        Mockito.when(userService.create(Mockito.any(User.class))).thenReturn(out);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated());

                ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userService).create(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("login123");
    }
}


