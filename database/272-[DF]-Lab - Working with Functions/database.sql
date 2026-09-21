sudo su
cd /home/ec2-user/
mysql -u root --password='re:St@rt!9'

SHOW DATABASES;

SELECT * FROM world.country;

SELECT sum(Population), avg(Population), max(Population), min(Population), count(Population) FROM world.country;

SELECT Region, substring_index(Region, " ", 1) FROM world.country;

SELECT Name, Region from world.country WHERE substring_index(Region, " ", 1) = "Southern";

SELECT Region FROM world.country WHERE LENGTH(TRIM(Region)) < 10;

SELECT DISTINCT(Region) FROM world.country WHERE LENGTH(TRIM(Region)) < 10;

SELECT Name, substring_index(Region, "/", 1) as "Region Name 1",substring_index(region, "/", -1) as "Region Name 2" FROM world.country WHERE Region = "Micronesia/Caribbean";