-- MPA ratings
INSERT INTO mpa_ratings (id, code, label) VALUES
(1,'G','General audiences'),
(2,'PG','Parental guidance suggested'),
(3,'PG-13','Parents strongly cautioned'),
(4,'R','Restricted'),
(5,'NC-17','Adults Only');

-- Genres
INSERT INTO genres (id, name) VALUES
(1,'Комедия'),
(2,'Драма'),
(3,'Мультфильм'),
(4,'Триллер'),
(5,'Документальный'),
(6,'Боевик');

-- Тестовые пользователи
INSERT INTO users (email, login, name, birthday)
VALUES ('user1@mail.ru','user1','Иван Иванов','1990-01-01'),
       ('user2@mail.ru','user2','Петр Петров','1992-05-05');

-- Тестовые фильмы
INSERT INTO films (name, description, release_date, duration_min, mpa_id)
VALUES ('Матрица','Фантастический боевик','1999-03-31',136,4),
       ('Титаник','Драма о любви и катастрофе','1997-12-19',195,3);
