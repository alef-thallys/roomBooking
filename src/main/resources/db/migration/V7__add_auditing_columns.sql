ALTER TABLE `room_booking`.`users`
    ADD COLUMN `created_by` VARCHAR(255),
    ADD COLUMN `created_date` DATETIME,
    ADD COLUMN `last_modified_by` VARCHAR(255),
    ADD COLUMN `last_modified_date` DATETIME;

ALTER TABLE `room_booking`.`rooms`
    ADD COLUMN `created_by` VARCHAR(255),
    ADD COLUMN `created_date` DATETIME,
    ADD COLUMN `last_modified_by` VARCHAR(255),
    ADD COLUMN `last_modified_date` DATETIME;

ALTER TABLE `room_booking`.`reservations`
    ADD COLUMN `created_by` VARCHAR(255),
    ADD COLUMN `created_date` DATETIME,
    ADD COLUMN `last_modified_by` VARCHAR(255),
    ADD COLUMN `last_modified_date` DATETIME;