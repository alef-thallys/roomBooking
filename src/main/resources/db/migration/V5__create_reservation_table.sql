CREATE TABLE IF NOT EXISTS `room_booking`.`reservations`
(
    id
    SERIAL
    PRIMARY
    KEY,
    start_date
    VARCHAR
(
    255
) NOT NULL,
    end_date VARCHAR
(
    255
) NOT NULL,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    CONSTRAINT fk_user
    FOREIGN KEY
(
    user_id
)
    REFERENCES users
(
    id
)
    ON DELETE CASCADE,
    CONSTRAINT fk_room
    FOREIGN KEY
(
    room_id
)
    REFERENCES rooms
(
    id
)
    ON DELETE CASCADE
    );
