CREATE TABLE IF NOT EXISTS `room_booking`.`rooms`
(
    `id`
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    `name`
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    `description` VARCHAR
(
    255
),
    `capacity` INT NOT NULL CHECK
(
    `capacity` >
    0
),
    `available` BOOLEAN NOT NULL DEFAULT TRUE,
    `location` VARCHAR
(
    255
) NOT NULL
    );
