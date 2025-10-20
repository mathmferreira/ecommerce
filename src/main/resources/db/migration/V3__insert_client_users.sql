-- Client 1
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),
           'client1@ecommerce.com',
           '$2a$10$sT1eQ0kT3FPRCqFHUsWwKuVRXmRfP6hhCQ7hFT.WzhjNvLZz4cfsO',  -- client1
           'Client 1',
           'USER',
           TRUE
       );

-- Client 2
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),
           'client2@ecommerce.com',
           '$2a$10$XGq7YN8qHQJ5P.9KF5KqVOXQJ5Yt5vKp5m5Q5m5Q5m5Q5m5Q5m5Q5',  -- client2
           'Client 2',
           'USER',
           TRUE
       );

-- Client 3
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),
           'client3@ecommerce.com',
           '$2a$10$YHr8ZO9rIRK6Q.0LG6LrWPYRK6Zu6wLq6n6R6n6R6n6R6n6R6n6R6',  -- client3
           'Client 3',
           'USER',
           TRUE
       );

-- Client 4
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),
           'client4@ecommerce.com',
           '$2a$10$ZIs9AP0sJSL7R.1MH7MsXQZSL7Av7xMr7o7S7o7S7o7S7o7S7o7S7',  -- client4
           'Client 4',
           'USER',
           TRUE
       );

-- Client 5
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),
           'client5@ecommerce.com',
           '$2a$10$aJt0BQ1tKTM8S.2NI8NtYRaTM8Bw8yNs8p8T8p8T8p8T8p8T8p8T8',  -- client5
           'Client 5',
           'USER',
           TRUE
       );

-- Client 6
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),
           'client6@ecommerce.com',
           '$2a$10$bKu1CR2uLUN9T.3OJ9OuZSbUN9Cx9zOt9q9U9q9U9q9U9q9U9q9U9',  -- client6
           'Client 6',
           'USER',
           TRUE
       );

-- Client 7
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),
           'client7@ecommerce.com',
           '$2a$10$cLv2DS3vMVO0U.4PK0PvaTcVO0Dy0APu0r0V0r0V0r0V0r0V0r0V0',  -- client7
           'Client 7',
           'USER',
           TRUE
       );

-- Client 8
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),
           'client8@ecommerce.com',
           '$2a$10$dMw3ET4wNWP1V.5QL1QwbUdWP1Ez1BQv1s1W1s1W1s1W1s1W1s1W1',  -- client8
           'Client 8',
           'USER',
           TRUE
       );

-- Client 9
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),
           'client9@ecommerce.com',
           '$2a$10$eNx4FU5xOXQ2W.6RM2RxcVeXQ2FA2CRw2t2X2t2X2t2X2t2X2t2X2',  -- client9
           'Client 9',
           'USER',
           TRUE
       );

-- Client 10
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),
           'client10@ecommerce.com',
           '$2a$10$fOy5GV6yPYR3X.7SN3SydWfYR3GB3DSx3u3Y3u3Y3u3Y3u3Y3u3Y3',  -- client10
           'Client 10',
           'USER',
           TRUE
       );