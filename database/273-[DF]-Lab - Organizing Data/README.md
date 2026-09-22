# Organizing Data — AWS Lab Guide

## Overview

This lab demonstrates how to use common database functions with the `GROUP BY`
and `OVER` clauses in the **world** database.
The database contains three tables: `city`, `country`, and `countrylanguage`.

---

## Objectives

By completing this lab, you will be able to:

- Use the `GROUP BY` clause with the aggregate function `SUM()`
- Use the `OVER` clause with the `RANK()` window function
- Use the `OVER` clause with the aggregate function `SUM()` and the `RANK()` window function

---

## Duration

**Approximately 45 minutes**

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

### Filter and Order Records by Region

> Returns all countries in the `Australia and New Zealand` region,
> ordered by population from largest to smallest.

```sql
SELECT Region, Name, Population
FROM world.country
WHERE Region = 'Australia and New Zealand'
ORDER BY Population DESC;
```

---

### GROUP BY — Total Population per Region

> Groups the filtered records by region and applies `SUM()` to calculate
> the total population for that region.

```sql
SELECT Region, SUM(Population)
FROM world.country
WHERE Region = 'Australia and New Zealand'
GROUP BY Region
ORDER BY SUM(Population) DESC;
```

---

### OVER — Running Total Using a Window Function

> Uses the `OVER()` clause to partition records by region and calculate
> a running total — each row shows its own population alongside the
> cumulative total up to that point.

```sql
SELECT
  Region,
  Name,
  Population,
  SUM(Population) OVER(PARTITION BY Region ORDER BY Population) AS 'Running Total'
FROM world.country
WHERE Region = 'Australia and New Zealand';
```

---

### OVER + RANK() — Running Total with Ranking

> Extends the previous query by adding the `RANK()` window function,
> which assigns a rank to each country within the region based on population.

```sql
SELECT
  Region,
  Name,
  Population,
  SUM(Population) OVER(PARTITION BY Region ORDER BY Population) AS 'Running Total',
  RANK() OVER(PARTITION BY Region ORDER BY Population)          AS 'Ranked'
FROM world.country
WHERE Region = 'Australia and New Zealand';
```

---

## Challenge

> **Rank the countries in each region by their population from largest to smallest.**

```sql
SELECT
  Region,
  Name,
  Population,
  RANK() OVER(PARTITION BY Region ORDER BY Population DESC) AS 'Rank'
FROM world.country
ORDER BY Region, Rank;
```

---

## Summary of Clauses and Functions Used

| Clause / Function                  | Description                                                         |
|------------------------------------|---------------------------------------------------------------------|
| `GROUP BY`                         | Groups rows that share a value into summary rows                    |
| `OVER(PARTITION BY ... ORDER BY …)`| Defines a window of rows for a window function to operate on        |
| `SUM(col)`                         | Calculates the total of a numeric column                            |
| `RANK()`                           | Assigns a rank to each row within a partition based on order        |
| `ORDER BY ... DESC`                | Sorts results in descending order                                   |

---

## Key Notes

- `GROUP BY` collapses multiple rows into a single summary row per group —
  you lose access to individual row details after grouping
- `OVER()` (window functions) keep all individual rows visible while still
  performing aggregate calculations across a defined partition
- `RANK()` assigns the same rank to tied values and skips the next rank
  (e.g. 1, 2, 2, 4 — rank 3 is skipped)
- `PARTITION BY` inside `OVER()` resets the calculation for each group,
  similar to how `GROUP BY` works but without collapsing rows
- Window functions cannot be used directly in a `WHERE` clause —
  use a subquery or CTE if filtering on a window function result is needed

---

## Additional Resources

- [GROUP BY Clause](https://dev.mysql.com/doc/refman/8.0/en/group-by-handling.html)
- [OVER Clause](https://dev.mysql.com/doc/refman/8.0/en/window-functions-usage.html)
- [SUM Function](https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_sum)
- [RANK Function](https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_rank)
- [SELECT Statements](https://dev.mysql.com/doc/refman/8.0/en/select.html)
- [COUNT Function](https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_count)
