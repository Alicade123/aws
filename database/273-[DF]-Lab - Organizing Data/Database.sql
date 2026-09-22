sudo su

cd /home/ec2-user/

mysql -u root --password='re:St@rt!9'

SHOW DATABASES;

SELECT * FROM world.country;

SELECT Region, Name, Population FROM world.country WHERE Region = 'Australia and New Zealand' ORDER By Population desc;

SELECT Region, SUM(Population) FROM world.country WHERE Region = 'Australia and New Zealand' GROUP By Region ORDER By SUM(Population) desc;

SELECT Region, Name, Population, SUM(Population) OVER(partition by Region ORDER BY Population) as 'Running Total' FROM world.country WHERE Region = 'Australia and New Zealand';

SELECT Region, Name, Population, SUM(Population) OVER(partition by Region ORDER BY Population) as 'Running Total', RANK() over(partition by region ORDER BY population) as 'Ranked' FROM world.country WHERE region = 'Australia and New Zealand';