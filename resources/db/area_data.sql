-- name: raw-area_data-count
-- Counts all the area_data
SELECT count(*) AS count
FROM area_data

-- name: raw-area_data-all
-- Get all area_data
SELECT * FROM area_data

-- name: raw-area_data-get
-- get area_data by id
SELECT * FROM area_data WHERE id = :id

-- name: raw-area_data-save!
-- update area_data
UPDATE area_data
SET name = :name,
    address = :address,
    max_area = :max_area
WHERE id = :id

-- name: raw-area_data-create<!
-- create new area_data
INSERT INTO area_data
       (max_area adress name)
VALUES (:max_area :adress :name)

-- name: raw-area_data-delete!
-- delete area_data
DELETE FROM area_data
WHERE id = :id
