# 🎬 Filmorate

REST-сервис для работы с фильмами и пользователями: лайки, дружеские связи, жанры и возрастные рейтинги, подсчёт популярных фильмов.

> Проект курса «Java-разработчик» Яндекс.Практикума.

## Стек

![Java](https://img.shields.io/badge/Java-21-6DB33F?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat&logo=spring&logoColor=white)
![JDBC](https://img.shields.io/badge/Spring%20JdbcTemplate-SQL-blue?style=flat)
![H2](https://img.shields.io/badge/H2-PostgreSQL%20mode-orange?style=flat)
![Lombok](https://img.shields.io/badge/Lombok-red?style=flat)
![JUnit 5](https://img.shields.io/badge/JUnit-5-green?style=flat&logo=junit5&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=flat&logo=githubactions&logoColor=white)

## Возможности

- CRUD для фильмов и пользователей с валидацией входных данных
- Лайки фильмов и вывод топа популярных
- Дружеские связи с заявками (статусы `PENDING` / `CONFIRMED`)
- Справочники жанров и возрастных рейтингов MPA (`G`, `PG`, `PG-13`, `R`, `NC-17`)
- Связь «фильм ↔ жанр» (многие-ко-многим)

## Архитектура

Слои `controller → service → storage`: хранилища реализованы в двух вариантах (in-memory и на `JdbcTemplate`), доступ к БД через `RowMapper`-ы. Схема БД описана в SQL-скриптах:

- [`src/main/resources/schema.sql`](src/main/resources/schema.sql) — таблицы, ключи, индексы, ограничения
- [`src/main/resources/data.sql`](src/main/resources/data.sql) — тестовые данные справочников

### ER-диаграмма

![ER-диаграмма](docs/filmorate-er.png)

Основные сущности: `users`, `films`, `friendships`, `film_likes`, `film_genres`, `genres`, `mpa_ratings`.

## Запуск

```bash
mvn spring-boot:run
# API: http://localhost:8080
```

## Тесты

- Юнит-тесты контроллеров и валидации
- Интеграционные тесты хранилищ и контроллеров (`@SpringBootTest`)

```bash
mvn test
```

## CI

На pull request запускается reusable workflow с API-тестами (Postman-коллекции через Newman) и проверкой Checkstyle.
