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

-- name: raw-surfaces-get-by-experiment
-- get surface by id
SELECT * FROM surfaces WHERE experiment = :experiment_id

-- name: raw-add-mesurment-to-surfaces<!
-- add mesurment to surface
INSERT INTO mesurments
       (surface, success)
VALUES (:surface_id, :success)

-- name: raw-all-mesurments-for-surfaces
-- get all mesurments for surfaces
SELECT * FROM mesurments
WHERE surfaces = :surfaces_id


-- name: raw-all-mesurments-for-experiment
-- get all mesurments for experiment
SELECT *
FROM surfaces
INNER JOIN mesurments
ON surfaces.id = mesurments.surface
WHERE surfaces.experiment = :experiment_id


-- name: raw-surfaces-delete!
-- delete surface
DELETE FROM surfaces
WHERE id = :id
