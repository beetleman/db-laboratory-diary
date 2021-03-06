-- name: raw-users-count
-- Counts all the users
SELECT count(*) AS count
FROM users

-- name: raw-users-all
-- Get all users
SELECT * FROM users

-- name: raw-users-get
-- get user by id
SELECT * FROM users WHERE id = :id

-- name: raw-users-get-by-username
-- get user by username
SELECT * FROM users WHERE username = :username

-- name: raw-users-save!
-- update user
UPDATE users
SET name = :name,
    firstname = :firstname,
    lastname = :lastname,
    username = :username,
    email = :email,
    password = :password,
    is_admin = :is_admin
WHERE id = :id

-- name: raw-users-create<!
-- create new user
INSERT INTO users
       (firstname, lastname, username, email, password, is_admin)
VALUES (:firstname, :lastname, :username, :email, :password, :is_admin)

-- name: raw-users-delete!
-- delete user
DELETE FROM users
WHERE id = :id
