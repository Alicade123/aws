sudo su
cd /home/ec2-user/
mysql -u root --password='re:St@rt!9'

SHOW DATABASES;

SELECT * FROM world.country;

SELECT Name, Capital, Region, SurfaceArea, Population FROM world.country WHERE Population >= 50000000 AND Population <= 100000000;

SELECT Name, Capital, Region, SurfaceArea, Population FROM world.country WHERE Population BETWEEN 50000000 AND 100000000;

SELECT sum(Population) from world.country WHERE Region LIKE "%Europe%";

SELECT sum(population) as "Europe Population Total" from world.country WHERE region LIKE "%Europe%";

SELECT Name, Capital, Region, SurfaceArea, Population from world.country WHERE LOWER(Region) LIKE "%central%";

SELECT SUM(SurfaceArea) as "N. America Surface Area", SUM(Population) as "N. America Population" FROM world.country WHERE Region = "North America";