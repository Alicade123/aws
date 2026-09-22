# Introduction to Amazon Aurora

## Overview

This lab introduces Amazon Aurora and provides a basic understanding of how
to create an Aurora instance, connect to it via an EC2 instance, and run
SQL queries against it.

---

## Topics Covered

By completing this lab, you will be able to:

- Create an Aurora instance
- Connect to a pre-created Amazon EC2 instance
- Configure the Amazon EC2 instance to connect to Aurora
- Query the Aurora instance

---

## Duration

**Approximately 40 minutes**

---

## Prerequisites

- Basic experience with the Linux operating system
- Basic understanding of SQL (Structured Query Language)

---

## Technologies Introduced

### Amazon Aurora
A fully managed, MySQL-compatible relational database engine that combines
the performance and reliability of high-end commercial databases with the
simplicity and cost-effectiveness of open-source databases. Delivers up to
**5x the performance of MySQL** without requiring changes to most existing
MySQL applications.

### Amazon EC2
A web service that provides resizable compute capacity in the cloud, reducing
the time required to provision new server instances to minutes.

### Amazon RDS
Makes it easy to set up, operate, and scale a relational database in the cloud.
Supports six database engines: **Aurora, Oracle, Microsoft SQL Server,
PostgreSQL, MySQL, and MariaDB**.

---

## Getting Started

### Access the AWS Management Console

1. Choose **Start Lab** at the upper-right corner
2. Wait for the circle next to **AWS** to turn **green** (lab is ready)
3. Click the green circle to open the **AWS Management Console**

> Do not change the lab Region unless specifically instructed to do so.

---

## Task 1: Create an Aurora Instance

1. In the search bar, type **RDS** and select it
2. In the left navigation menu, choose **Databases**
3. Choose **Create database** and configure the following:

### Database Creation Method

| Field                   | Value                        |
|-------------------------|------------------------------|
| Creation method         | `Standard create`            |
| Engine type             | `Aurora (MySQL Compatible)`  |
| Engine version          | Default for major version `8.0` |
| Template                | `Dev/Test`                   |

### Settings

| Field                 | Value       |
|-----------------------|-------------|
| DB cluster identifier | `aurora`    |
| Master username       | `admin`     |
| Master password       | `admin123`  |
| Confirm password      | `admin123`  |

### Instance Configuration

| Field           | Value                                    |
|-----------------|------------------------------------------|
| DB instance class | `Burstable classes (includes t classes)` |
| Instance size   | `db.t3.medium`                           |

### Availability & Durability

| Field             | Value                          |
|-------------------|--------------------------------|
| Multi-AZ deployment | `Don't create an Aurora Replica` |

> In production, Multi-AZ is recommended. For this lab, a single instance is sufficient.

### Connectivity

| Field                | Value              |
|----------------------|--------------------|
| VPC                  | `LabVPC`           |
| Subnet group         | `dbsubnetgroup`    |
| Public access        | `No`               |
| VPC security group   | `Choose existing`  |
| Existing SG          | `DBSecurityGroup` (remove `default`) |

### Additional Settings to Disable / Configure

- Uncheck **Enable Enhanced Monitoring**
- Under **Additional configuration**, set **Initial database name** to `world`
- Uncheck **Enable encryption**
- Uncheck **Enable auto minor version upgrade**

4. Scroll to the bottom and choose **Create database**

> Aurora can take up to 5 minutes to launch — you can continue to the next task
> while it provisions.

---

## Task 2: Connect to the Amazon EC2 Linux Instance

1. In the search bar, type **EC2** and select it
2. In the left navigation menu, choose **Instances**
3. Select the checkbox next to **Command Host** → choose **Connect**
4. Choose the **Session Manager** tab → click **Connect**

> If the Connect button is not available, wait a few minutes and try again.

---

## Task 3: Configure the EC2 Instance to Connect to Aurora

### Step 1 — Install the MariaDB Client

Run the following command in the Session Manager terminal:

```bash
sudo yum install mariadb -y
```

Expected output (truncated):

```
Install  1 Package
Total download size: 8.8 M
Installed size: 49 M
...
Complete!
```

### Step 2 — Get the Aurora Endpoint

1. Go back to the AWS Management Console → **RDS → Databases**
2. Wait for `aurora-instance-1` to show status **Available**
3. Choose **aurora** → open the **Connectivity & security** tab
4. Copy the **Endpoint name** for the **Writer** instance

The endpoint will look similar to:
```
aurora.cluster-cabcdefghijklm.us-west-2.rds.amazonaws.com
```

> Save this in a text editor — you will use it in the next step.

### Aurora Endpoint Types

| Endpoint Type    | Purpose                                                  |
|------------------|----------------------------------------------------------|
| Cluster endpoint | Connects to the primary DB; handles all write operations |
| Reader endpoint  | Load-balances read-only connections across replicas      |

### Step 3 — Connect to Aurora

Replace `<endpoint_goes_here>` with your copied endpoint and run:

```bash
mysql -u admin --password='admin123' -h <endpoint_goes_here>
```

Example:
```bash
mysql -u admin --password='admin123' -h aurora.cluster-123456789012.us-west-2.rds.amazonaws.com
```

| Flag          | Description                          |
|---------------|--------------------------------------|
| `-u`          | MySQL username                       |
| `--password`  | MySQL password                       |
| `-h`          | Host address of the database engine  |

Expected output:

```
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MySQL connection id is 173
Server version: 8.0.28 Source distribution
Type 'help;' or '\h' for help.
MySQL [(none)]>
```

---

## Task 4: Create a Table, Insert and Query Records

### Show Available Databases

```sql
SHOW DATABASES;
```

Expected output:

```
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| sys                |
| world              |
+--------------------+
5 rows in set (0.02 sec)
```

---

### Switch to the World Database

```sql
USE world;
```

Expected output:

```
Database changed
MySQL [world]>
```

---

### Create the Country Table

```sql
CREATE TABLE `country` (
  `Code`            CHAR(3)      NOT NULL DEFAULT '',
  `Name`            CHAR(52)     NOT NULL DEFAULT '',
  `Continent`       ENUM('Asia','Europe','North America','Africa',
                    'Oceania','Antarctica','South America')
                                 NOT NULL DEFAULT 'Asia',
  `Region`          CHAR(26)     NOT NULL DEFAULT '',
  `SurfaceArea`     FLOAT(10,2)  NOT NULL DEFAULT '0.00',
  `IndepYear`       SMALLINT(6)           DEFAULT NULL,
  `Population`      INT(11)      NOT NULL DEFAULT '0',
  `LifeExpectancy`  FLOAT(3,1)            DEFAULT NULL,
  `GNP`             FLOAT(10,2)           DEFAULT NULL,
  `GNPOld`          FLOAT(10,2)           DEFAULT NULL,
  `LocalName`       CHAR(45)     NOT NULL DEFAULT '',
  `GovernmentForm`  CHAR(45)     NOT NULL DEFAULT '',
  `Capital`         INT(11)               DEFAULT NULL,
  `Code2`           CHAR(2)      NOT NULL DEFAULT '',
  PRIMARY KEY (`Code`)
);
```

Expected output:

```
Query OK, 0 rows affected, 7 warnings (0.02 sec)
```

---

### Insert Records into the Country Table

```sql
INSERT INTO `country` VALUES ('GAB','Gabon','Africa','Central Africa',267668.00,1960,1226000,50.1,5493.00,5279.00,'Le Gabon','Republic',902,'GA');

INSERT INTO `country` VALUES ('IRL','Ireland','Europe','British Islands',70273.00,1921,3775100,76.8,75921.00,73132.00,'Ireland/Éire','Republic',1447,'IE');

INSERT INTO `country` VALUES ('THA','Thailand','Asia','Southeast Asia',513115.00,1350,61399000,68.6,116416.00,153907.00,'Prathet Thai','Constitutional Monarchy',3320,'TH');

INSERT INTO `country` VALUES ('CRI','Costa Rica','North America','Central America',51100.00,1821,4023000,75.8,10226.00,9757.00,'Costa Rica','Republic',584,'CR');

INSERT INTO `country` VALUES ('AUS','Australia','Oceania','Australia and New Zealand',7741220.00,1901,18886000,79.8,351182.00,392911.00,'Australia','Constitutional Monarchy, Federation',135,'AU');
```

Expected output per insert:

```
Query OK, 1 row affected (0.00 sec)
```

---

### Query the Table

```sql
SELECT * FROM country
WHERE GNP > 35000
  AND Population > 10000000;
```

Expected output:

```
+------+-----------+-----------+---------------------------+-------------+-----------+------------+----------------+-----------+-----------+--------------+------------------------------------+---------+-------+
| Code | Name      | Continent | Region                    | SurfaceArea | IndepYear | Population | LifeExpectancy | GNP       | GNPOld    | LocalName    | GovernmentForm                     | Capital | Code2 |
+------+-----------+-----------+---------------------------+-------------+-----------+------------+----------------+-----------+-----------+--------------+------------------------------------+---------+-------+
| AUS  | Australia | Oceania   | Australia and New Zealand |  7741220.00 |      1901 |   18886000 |           79.8 | 351182.00 | 392911.00 | Australia    | Constitutional Monarchy, Federation|     135 | AU    |
| THA  | Thailand  | Asia      | Southeast Asia            |   513115.00 |      1350 |   61399000 |           68.6 | 116416.00 | 153907.00 | Prathet Thai | Constitutional Monarchy            |    3320 | TH    |
+------+-----------+-----------+---------------------------+-------------+-----------+------------+----------------+-----------+-----------+--------------+------------------------------------+---------+-------+
2 rows in set (0.00 sec)
```

> The query returns two records: **Australia** and **Thailand** —
> both have a GNP greater than 35,000 and a population greater than 10,000,000.

---

## Summary of AWS Resources Used

| Resource         | Name / Value         | Purpose                                      |
|------------------|----------------------|----------------------------------------------|
| Aurora Cluster   | `aurora`             | Managed MySQL-compatible relational database  |
| Initial Database | `world`              | Database used for table creation and queries  |
| EC2 Instance     | `Command Host`       | Linux instance used as a database client      |
| VPC              | `LabVPC`             | Network environment for all resources         |
| Subnet Group     | `dbsubnetgroup`      | Private subnets assigned to the Aurora cluster|
| Security Group   | `DBSecurityGroup`    | Controls inbound access to Aurora             |

---

## Key Concepts

- **Aurora Cluster Endpoint** — used for all write operations (INSERT, UPDATE,
  DELETE, DDL); supports automatic failover
- **Aurora Reader Endpoint** — load-balances read-only queries across available
  replicas
- **DB Subnet Group** — a collection of private subnets designated for RDS/Aurora
  instances within a VPC
- **MariaDB client** — a MySQL-compatible CLI tool used to connect to and
  interact with Aurora from the EC2 instance

---

## Additional Resources

- [Amazon Aurora Documentation](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/CHAP_AuroraOverview.html)
- [Aurora Endpoints](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/Aurora.Overview.Endpoints.html)
- [Amazon RDS Documentation](https://docs.aws.amazon.com/rds/)
- [Amazon EC2 Documentation](https://docs.aws.amazon.com/ec2/)
- [MySQL SELECT Statement](https://dev.mysql.com/doc/refman/8.0/en/select.html)
- [AWS Training and Certification](https://aws.amazon.com/training/)