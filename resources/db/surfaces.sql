-- name: raw-area_data-count
-- Counts all the surfaces
SELECT count(*) AS count
FROM surfaces

-- name: raw-surfaces-all
-- Get all surfaces
SELECT * FROM surfaces

-- name: raw-surfaces-get
-- get surface by id
SELECT * FROM surfaces WHERE id = :id

-- name: raw-add-mesurment-to-surfaces<!
-- add mesurment to surface
INSERT INTO mesurments
       (surface, success)
VALUES (:surface_id, :success)

-- name: raw-all-mesurments-for-surfaces
-- get all mesurments for surfaces
SELECT * FROM mesurments
WHERE surfaces = :surfaces_id


-- name: raw-surfaces-delete!
-- delete surface
DELETE FROM surfaces
WHERE id = :id
