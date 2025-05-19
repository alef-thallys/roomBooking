CREATE TABLE IF NOT EXISTS `room_booking`.`users`
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
) NOT NULL,
    `email` VARCHAR
(
    255
) NOT NULL UNIQUE,
    `password` VARCHAR
(
    255
) NOT NULL,
    `phone` VARCHAR
(
    15
) NOT NULL,
    `role` VARCHAR
(
    50
) NOT NULL
    );
