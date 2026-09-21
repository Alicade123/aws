# Selecting Data from a Database — AWS Lab Guide

## Overview

This lab demonstrates how to query a relational database named **world** using MySQL.
The database contains three tables: `city`, `country`, and `countrylanguage`.

---

## Objectives

By completing this lab, you will be able to:

- Use the `SELECT` statement to query a database
- Use the `COUNT()` function
- Use the following operators:
  - `<` `>` `=`
  - `WHERE`
  - `ORDER BY`
  - `AND`

---

## Duration

---

## Prerequisites

- Access to AWS Management Console
- An active lab session with a provisioned **Command Host** EC2 instance
- The `world` database pre-installed on the instance

---

## Getting Started

### Step 1: Access the AWS Management Console

1. Choose **Start Lab** at the upper-right corner
2. Wait for the circle next to **AWS** to turn **green** (lab is ready)
3. Click the green circle to open the **AWS Management Console**

---

## Task 1: Connect to the Command Host

1. Go to **Services → Compute → EC2**
2. In the left menu, choose **Instances**
3. Select the **Command Host** instance checkbox
4. Click **Connect**
5. Choose the **Session Manager** tab → Click **Connect**

Once connected, run:

```bash
sudo su
cd /home/ec2-user/
```

### Connect to the Database

```bash
mysql -u root --password='re:St@rt!9'
```

> **Reconnect tip:** If the Session Manager becomes unresponsive, close it, reconnect,
> and re-run the commands above.

---

## Task 2: Query the World Database

### Show Available Databases

```sql
SHOW DATABASES;
```

---

### View All Rows in the Country Table

```sql
SELECT * FROM world.country;
```

---

### Count All Rows in the Country Table

```sql
SELECT COUNT(*) FROM world.country;
```

---

### Inspect the Table Schema

```sql
SHOW COLUMNS FROM world.country;
```

---

### Select Specific Columns

```sql
SELECT Name, Capital, Region, SurfaceArea, Population
FROM world.country;
```

---

### Rename a Column with AS

```sql
SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population
FROM world.country;
```

---

### Order Results by Population (Ascending)

```sql
SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population
FROM world.country
ORDER BY Population;
```

---

### Order Results by Population (Descending)

```sql
SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population
FROM world.country
ORDER BY Population DESC;
```

---

### Filter with WHERE — Population > 50,000,000

```sql
SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population
FROM world.country
WHERE Population > 50000000
ORDER BY Population DESC;
```

---

### Filter with WHERE + AND — Population Between 50M and 100M

```sql
SELECT Name, Capital, Region, SurfaceArea AS "Surface Area", Population
FROM world.country
WHERE Population > 50000000
  AND Population < 100000000
ORDER BY Population DESC;
```

---

## Challenge

> **Which country in Southern Europe has a population greater than 50,000,000?**

```sql
SELECT Name, Region, Population
FROM world.country
WHERE Region = 'Southern Europe'
  AND Population > 50000000;
```

---

## Summary of Operators Used

| Operator   | Description                          |
|------------|--------------------------------------|
| `SELECT`   | Retrieve data from a table           |
| `COUNT()`  | Count rows in a result set           |
| `WHERE`    | Filter rows based on a condition     |
| `ORDER BY` | Sort results ascending or descending |
| `AND`      | Combine multiple conditions          |
| `>`        | Greater than                         |
| `<`        | Less than                            |
| `=`        | Equal to                             |
| `AS`       | Rename a column in the output        |
| `DESC`     | Sort in descending order             |

---

## Additional Resources

- [SELECT Statements](https://dev.mysql.com/doc/refman/8.0/en/select.html)
- [COUNT Function](https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_count)
- [ORDER BY](https://dev.mysql.com/doc/refman/8.0/en/sorting-rows.html)
- [Comparison Operators](https://dev.mysql.com/doc/refman/8.0/en/comparison-operators.html)
- [AWS Training and Certification](https://aws.amazon.com/training/)

---

## License & Attribution

- Sample data sourced from [Statistics Finland](https://stat.fi/), downloaded February 4, 2022

---

*Built for AWS Training & Certification lab practice.*
