# Performing a Conditional Search — AWS Lab Guide

## Overview

This lab demonstrates how to use the `SELECT` statement and a `WHERE` clause
to filter records with a conditional search in the **world** database.
The database contains three tables: `city`, `country`, and `countrylanguage`.

---

## Objectives

By completing this lab, you will be able to:

- Write a search condition using the `WHERE` clause
- Use the `BETWEEN` operator
- Use the `LIKE` operator with wildcard characters
- Use the `AS` operator to create a column alias
- Use functions in a `SELECT` statement
- Use functions in a `WHERE` clause

---

## Duration

Approximately 45 minutes

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

> **Reconnect tip:** If the Session Manager becomes unresponsive, close it,
> reconnect, and re-run the commands above.

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

### Filter with WHERE + AND — Population Between 50M and 100M

```sql
SELECT Name, Capital, Region, SurfaceArea, Population
FROM world.country
WHERE Population >= 50000000
  AND Population <= 100000000;
```

---

### Same Filter Using BETWEEN (More Readable)

> The `BETWEEN` operator is **inclusive** — both boundary values are included.

```sql
SELECT Name, Capital, Region, SurfaceArea, Population
FROM world.country
WHERE Population BETWEEN 50000000 AND 100000000;
```

---

### Total Population of All European Countries Using LIKE + SUM

> `%` is a wildcard that matches any number of characters before or after the word.

```sql
SELECT SUM(Population)
FROM world.country
WHERE Region LIKE "%Europe%";
```

---

### Same Query with a Column Alias Using AS

```sql
SELECT SUM(Population) AS "Europe Population Total"
FROM world.country
WHERE Region LIKE "%Europe%";
```

---

### Case-Insensitive Search Using LOWER()

> Use `LOWER()` to handle databases configured with case-sensitive collation.

```sql
SELECT Name, Capital, Region, SurfaceArea, Population
FROM world.country
WHERE LOWER(Region) LIKE "%central%";
```

---

## Challenge

> **Write a query to return the sum of the surface area and sum of the
> population of North America.**

```sql
SELECT
  SUM(SurfaceArea) AS "Total Surface Area",
  SUM(Population)  AS "Total Population"
FROM world.country
WHERE Region = 'North America';
```

---

## Summary of Operators & Functions Used

| Operator / Function | Description                                          |
|---------------------|------------------------------------------------------|
| `WHERE`             | Filter rows based on a condition                     |
| `AND`               | Combine multiple conditions                          |
| `>=` / `<=`         | Greater than or equal / Less than or equal           |
| `BETWEEN`           | Filter within an inclusive range                     |
| `LIKE`              | Match a string pattern                               |
| `%`                 | Wildcard — matches any number of characters          |
| `SUM()`             | Calculate the total of a numeric column              |
| `AS`                | Rename a column in the output (alias)                |
| `LOWER()`           | Convert a string to lowercase for comparison         |

---

## Key Notes

- SQL is **not case-sensitive** for keywords (`SELECT` = `select`)
- However, **databases may be case-sensitive** depending on their collation config
- Always match the **naming convention** used in the database schema
- `BETWEEN` is inclusive — `BETWEEN 50 AND 100` includes both `50` and `100`
- `LIKE "%Europe%"` matches any region containing the word `Europe` anywhere

---

## Additional Resources

- [WHERE Clause](https://dev.mysql.com/doc/refman/8.0/en/where-optimization.html)
- [BETWEEN Operator](https://dev.mysql.com/doc/refman/8.0/en/comparison-operators.html#operator_between)
- [LIKE Function](https://dev.mysql.com/doc/refman/8.0/en/string-comparison-functions.html#operator_like)
- [SUM Function](https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_sum)
- [LOWER Function](https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_lower)
- [AWS Training and Certification](https://aws.amazon.com/training/)

---

## License & Attribution

- Sample data sourced from [Statistics Finland](https://stat.fi/),
  downloaded February 4, 2022
- License: [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)
- © 2022 Amazon Web Services, Inc. All rights reserved.

---

*Built for AWS Training & Certification lab practice.*
