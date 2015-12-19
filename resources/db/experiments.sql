-- name: raw-experiments-count
-- Counts all the experiments
SELECT count(*) AS count
FROM experiments

-- name: raw-experiments-all
-- Get all experiments
SELECT * FROM experiments

-- name: raw-experiments-get
-- get experiment by id
SELECT * FROM experiments WHERE id = :id

-- name: raw-experiments-save!
-- update experiment
UPDATE experiments
SET manager = :manager_id,
    area_data = :area_data_id,
    fertilizer = :fertilizer,
    start_date = :start_date,
    stop_date = :stop_date
WHERE id = :id

-- name: raw-experiments-start!
-- start experiment
UPDATE experiments
SET start_date = :start_date
WHERE id = :id

-- name: raw-experiments-stop!
-- stop experiment
UPDATE experiments
SET stop_date = :stop_date
WHERE id = :id

-- name: raw-add-laborant-to-experiment<!
-- add laborant to experiment
INSERT INTO laborants_experiments
(experiment, laborant)
VALUES (:experiment_id, :laborant_id)

-- name: raw-delete-laborant-from-experiment!
-- delete laborant to experiment
DELETE FROM laborants_experiments
where experiment = :experiment_id and laborant = :laborant_id

-- name: raw-all-laborant-for-experiment
-- get all laborant for experiment
SELECT users.id, users.firstname, users.lastname, users.login,
       users.email, users.password, users.is_admin
FROM users
INNER JOIN laborants_experiments
      ON users.id = laborants_experiments.laborant
WHERE laborants_experiments.experiment = :experiment_id

-- name: raw-all-experiments-for-laborant
-- get all experiments for user
SELECT experiments.id, experiments.manager, experiments.area_data,
       experiments.fertilizer, experiments.start_date,
       experiments.stop_date, experiments.create_data
FROM experiments
INNER JOIN laborants_experiments
      ON experiments.id = laborants_experiments.experiment
WHERE laborants_experiments.experiment = :experiment_id

-- name: raw-experiments-create<!
-- create new experiment
INSERT INTO experiments
(manager, area_data, fertilizer)
VALUES (:manager_id, :area_data_id, :fertilizer)

-- name: raw-experiments-delete!
-- delete experiment
DELETE FROM experiments
WHERE id = :id
