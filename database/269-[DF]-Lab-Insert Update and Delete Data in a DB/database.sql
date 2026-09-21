INSERT INTO
    world.country
VALUES
    (
        'IRL',
        'Ireland',
        'Europe',
        'British Islands',
        70273.00,
        1921,
        3775100,
        76.8,
        75921.00,
        73132.00,
        'Ireland/Éire',
        'Republic',
        1447,
        'IE'
    );

INSERT INTO
    world.country
VALUES
    (
        'AUS',
        'Australia',
        'Oceania',
        'Australia and New Zealand',
        7741220.00,
        1901,
        18886000,
        79.8,
        351182.00,
        392911.00,
        'Australia',
        'Constitutional Monarchy, Federation',
        135,
        'AU'
    );

SELECT * FROM world.country WHERE Code IN ('IRL', 'AUS');

UPDATE world.country SET Population = 0;

UPDATE world.country SET Population = 100, SurfaceArea = 100;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM world.country;

QUIT;

ls /home/ec2-user/world.sql

mysql -u root --password='re:St@rt!9' < /home/ec2-user/world.sql

mysql -u root --password='re:St@rt!9'

USE world;
SHOW TABLES;

SELECT * FROM country;