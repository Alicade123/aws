sudo su
cd /home/ec2-user/
mysql -u root --password='re:St@rt!9'

SHOW DATABASES;

SELECT * FROM world.country;

SELECT COUNT(*) FROM world.country;

SHOW COLUMNS FROM world.country;

SELECT Name, Capital, Region, SurfaceArea, Population FROM world.country;

SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population FROM world.country;

SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population FROM world.country ORDER BY Population;

SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population FROM world.country ORDER BY Population DESC;

SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population FROM world.country WHERE Population > 50000000 ORDER BY Population DESC;

SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population FROM world.country WHERE Population > 50000000 ORDER BY Population DESC;

SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population from world.country WHERE Population > 50000000 AND Region = "Southern Europe";