# Working with Functions — AWS Lab Guide

## Overview

This lab demonstrates how to use common database functions with the `SELECT`
statement and `WHERE` clause in the **world** database.
The database contains three tables: `city`, `country`, and `countrylanguage`.

---

## Objectives

By completing this lab, you will be able to:

- Use aggregate functions `SUM()`, `MIN()`, `MAX()`, and `AVG()` to summarize data
- Use the `SUBSTRING_INDEX()` function to split strings
- Use the `LENGTH()` and `TRIM()` functions to determine the length of a string
- Use the `DISTINCT()` function to filter duplicate records
- Use functions in the `SELECT` statement and `WHERE` clause

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

### Aggregate Functions — SUM, AVG, MAX, MIN, COUNT

> These functions aggregate data across **all rows** since no `WHERE` clause is used.

```sql
SELECT
  SUM(Population),
  AVG(Population),
  MAX(Population),
  MIN(Population),
  COUNT(Population)
FROM world.country;
```

| Function    | Description                                        |
|-------------|----------------------------------------------------|
| `SUM()`     | Adds all population values together                |
| `AVG()`     | Calculates the average across all population values|
| `MAX()`     | Finds the row with the highest population value    |
| `MIN()`     | Finds the row with the lowest population value     |
| `COUNT()`   | Counts the number of rows with a population value  |

---

### Split a String Using SUBSTRING_INDEX()

> Splits each region name at the first space, returning only the first word.

```sql
SELECT Region, SUBSTRING_INDEX(Region, " ", 1)
FROM world.country;
```

---

### Use SUBSTRING_INDEX() in a WHERE Clause

> Filters records where the first word of the region name is `Southern`.

```sql
SELECT Name, Region
FROM world.country
WHERE SUBSTRING_INDEX(Region, " ", 1) = "Southern";
```

---

### Filter by String Length Using LENGTH() and TRIM()

> `TRIM()` removes leading/trailing spaces before `LENGTH()` counts characters.

```sql
SELECT Region
FROM world.country
WHERE LENGTH(TRIM(Region)) < 10;
```

---

### Remove Duplicates Using DISTINCT()

> Wraps the previous query to return only unique region names.

```sql
SELECT DISTINCT(Region)
FROM world.country
WHERE LENGTH(TRIM(Region)) < 10;
```

---

## Challenge

> **Return rows where the region is `Micronesian/Caribbean`, split into two
> separate columns named `Region Name 1` and `Region Name 2`.**

```sql
SELECT
  SUBSTRING_INDEX(Region, "/", 1)  AS "Region Name 1",
  SUBSTRING_INDEX(Region, "/", -1) AS "Region Name 2"
FROM world.country
WHERE Region = "Micronesian/Caribbean";
```

---

## Summary of Functions Used

| Function              | Category    | Description                                        |
|-----------------------|-------------|----------------------------------------------------|
| `SUM(col)`            | Aggregate   | Total of all values in a column                    |
| `AVG(col)`            | Aggregate   | Average of all values in a column                  |
| `MAX(col)`            | Aggregate   | Highest value in a column                          |
| `MIN(col)`            | Aggregate   | Lowest value in a column                           |
| `COUNT(col)`          | Aggregate   | Number of non-null rows in a column                |
| `SUBSTRING_INDEX()`   | String      | Splits a string by a delimiter                     |
| `LENGTH(col)`         | String      | Returns the number of characters in a string       |
| `TRIM(col)`           | String      | Removes leading and trailing whitespace            |
| `DISTINCT(col)`       | Filter      | Removes duplicate values from the result set       |

---

## Key Notes

- Aggregate functions (`SUM`, `AVG`, `MAX`, `MIN`, `COUNT`) operate across
  **all rows** unless filtered with a `WHERE` clause
- `SUBSTRING_INDEX(str, delim, count)`:
  - Positive `count` → returns from the **left**
  - Negative `count` → returns from the **right**
- Always use `TRIM()` before `LENGTH()` to avoid counting accidental whitespace
- `DISTINCT()` eliminates duplicate rows from the result set
- SQL keywords are **not case-sensitive**, but follow the database naming convention

---

## Additional Resources

- [SELECT Statements](https://dev.mysql.com/doc/refman/8.0/en/select.html)
- [COUNT Function](https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_count)
- [SUM Function](https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_sum)
- [AVG Function](https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_avg)
- [MIN Function](https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_min)
- [MAX Function](https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_max)
- [SUBSTRING_INDEX Function](https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_substring-index)
- [LENGTH Function](https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_length)
- [TRIM Function](https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_trim)
- [AWS Training and Certification](https://aws.amazon.com/training/)

---

## License & Attribution

- Sample data sourced from [Statistics Finland](https://stat.fi/),
  downloaded February 4, 2022
- License: [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)
- © 2022 Amazon Web Services, Inc. All rights reserved.

---

*Built for AWS Training & Certification lab practice.*
