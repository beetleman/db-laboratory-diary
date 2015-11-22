CREATE TABLE users (
       id SERIAL PRIMARY KEY,
       firstname VARCHAR(100),
       lastname VARCHAR(100),
       login VARCHAR(100) NOT NULL,
       email VARCHAR(100),
       password VARCHAR(40) NOT NULL, -- sha1
       is_admin BOOLEAN NOT NULL DEFAULT TRUE
);
