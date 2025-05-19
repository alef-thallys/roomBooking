INSERT INTO users (name, email, password, phone, role)
VALUES ('John Doe', 'john.doe@example.com', '$2a$10$R1g9ADKpAqZtd6StmwPUPuMiv50xWoPxXLvWazc2Yu80InHy4yf.y', '12345678',
        'ROLE_ADMIN'),
       ('Jane Smith', 'jane.smith@example.com', '$2a$10$R1g9ADKpAqZtd6StmwPUPuMiv50xWoPxXLvWazc2Yu80InHy4yf.y',
        '23456789', 'ROLE_USER'),
       ('Alice Johnson', 'alice.johnson@example.com', '$2a$10$R1g9ADKpAqZtd6StmwPUPuMiv50xWoPxXLvWazc2Yu80InHy4yf.y',
        '34567890', 'ROLE_USER'),
       ('Bob Brown', 'bob.brown@example.com', '$2a$10$R1g9ADKpAqZtd6StmwPUPuMiv50xWoPxXLvWazc2Yu80InHy4yf.y',
        '45678901', 'ROLE_ADMIN');
