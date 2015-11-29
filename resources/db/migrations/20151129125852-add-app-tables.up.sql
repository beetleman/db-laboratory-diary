CREATE TABLE area_data (
       id SERIAL PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       address VARCHAR(200) NOT NULL,
       max_area INT NOT NULL
);
--;;
CREATE TABLE experiments (
       id SERIAL PRIMARY KEY,
       manager INT REFERENCES users(id),
       area_data INT REFERENCES area_data(id),
       fertilizer BOOLEAN NOT NULL,
       start_date DATE NOT NULL,
       stop_date DATE NOT NULL,
       create_data TIMESTAMP DEFAULT current_timestamp
);
--;;
CREATE TABLE laborants_experiments (
       id SERIAL PRIMARY KEY,
       experiment INT REFERENCES experiments(id),
       laborant INT REFERENCES users(id)
);
--;;
CREATE TABLE surfaces (
       id SERIAL PRIMARY KEY,
       experiment INT REFERENCES experiments(id),
       area INT NOT NULL
);
--;;
CREATE TABLE mesurments (
       id SERIAL PRIMARY KEY,
       surface INT REFERENCES surfaces(id),
       create_data TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       success BOOLEAN NOT NULL
);
