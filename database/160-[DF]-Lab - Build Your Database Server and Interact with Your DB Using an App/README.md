# Build Your DB Server and Interact With Your DB Using an App

## Overview

This lab reinforces the concept of leveraging an AWS-managed database instance
for solving relational database needs using **Amazon RDS**.

Amazon RDS makes it easy to set up, operate, and scale a relational database
in the cloud. It provides cost-efficient and resizable capacity while managing
time-consuming database administration tasks. Available engines include:
**Amazon Aurora, Oracle, Microsoft SQL Server, PostgreSQL, MySQL, and MariaDB.**

---

## Objectives

By completing this lab, you will be able to:

- Launch an Amazon RDS DB instance with high availability
- Configure the DB instance to permit connections from your web server
- Open a web application and interact with your database

---

## Duration

**Approximately 45 minutes**

---

## Architecture

**Starting infrastructure:**

```
Web Server (EC2)  →  No database connected yet
```

**Final infrastructure:**

```
Web Server (EC2)  →  Amazon RDS MySQL (Multi-AZ)
                          ├── Primary DB (AZ 1 - Private Subnet 1)
                          └── Standby DB (AZ 2 - Private Subnet 2)
```

---

## Getting Started

### Access the AWS Management Console

1. Choose **Start Lab** at the upper-right corner
2. Wait for the circle next to **AWS** to turn **green** (lab is ready)
3. Click the green circle to open the **AWS Management Console**

> Do not change the lab Region unless specifically instructed to do so.

---

## Task 1: Create a Security Group for the RDS DB Instance

> This security group controls which resources can connect to your RDS instance.

1. In the search bar, type **VPC** and select it
2. In the left navigation pane, choose **Security groups**
3. Click **Create security group** and configure:

| Field                | Value                          |
|----------------------|--------------------------------|
| Security group name  | `DB Security Group`            |
| Description          | `Permit access from Web Security Group` |
| VPC                  | `Lab VPC`                      |

4. Under **Inbound rules**, click **Add rule** and configure:

| Field        | Value                    |
|--------------|--------------------------|
| Type         | `MySQL/Aurora (3306)`    |
| Source type  | `Custom`                 |
| Source       | `Web Security Group`     |

> This allows inbound traffic on port **3306** from any EC2 instance
> associated with the Web Security Group.

5. Scroll to the bottom and click **Create security group**

---

## Task 2: Create a DB Subnet Group

> A DB subnet group tells RDS which subnets can be used for the database.
> At least two Availability Zones are required.

1. In the search bar, type **RDS** and select **Aurora and RDS**
2. In the left navigation pane, click **Subnet groups**
3. Click **Create DB Subnet Group** and configure:

| Field        | Value              |
|--------------|--------------------|
| Name         | `DB Subnet Group`  |
| Description  | `DB Subnet Group`  |
| VPC          | `Lab VPC`          |

4. Under **Add Subnets**, select the first and second Availability Zones
5. Select the following subnets:

| Subnet             | CIDR           |
|--------------------|----------------|
| Private Subnet 1   | `10.0.1.0/24`  |
| Private Subnet 2   | `10.0.3.0/24`  |

6. Click **Create**

---

## Task 3: Create an Amazon RDS DB Instance

> Multi-AZ deployments provide enhanced availability and durability.
> RDS automatically creates a primary instance and synchronously replicates
> data to a standby instance in a different Availability Zone.

1. In the left navigation pane, click **Databases**
2. Click the dropdown arrow on **Create database** → select **Full configuration**

### Engine Options

| Field           | Value    |
|-----------------|----------|
| Engine type     | `MySQL`  |
| Template        | `Dev/Test` |
| Availability    | `Multi-AZ DB instance deployment (2 instances)` |

### Settings

| Field                   | Value          |
|-------------------------|----------------|
| DB instance identifier  | `lab-db`       |
| Master username         | `main`         |
| Credentials management  | `Self managed` |
| Master password         | `lab-password` |
| Confirm master password | `lab-password` |

### Instance Configuration

| Field          | Value                               |
|----------------|-------------------------------------|
| Class type     | `Burstable classes (includes t classes)` |
| Instance size  | `db.t3.medium`                      |

### Storage

| Field             | Value                        |
|-------------------|------------------------------|
| Storage type      | `General Purpose SSD (gp3)`  |
| Allocated storage | `20 GB`                      |

### Connectivity

| Field              | Value                        |
|--------------------|------------------------------|
| Compute resource   | `Don't connect to an EC2 compute resource` |
| VPC                | `Lab VPC`                    |
| DB subnet group    | `DB Subnet Group`            |
| Public access      | `No`                         |
| VPC security group | `DB Security Group` (remove `default`) |

### Additional Settings to Disable

- Uncheck **Enable Enhanced Monitoring**
- Uncheck **Enable Performance Insights**
- Under **Additional configuration**, set **Initial database name** to `lab`
- Under **Backup**, uncheck **Enable automated backups**

> Disabling backups is not recommended in production — it is only done here
> to speed up the lab deployment.

3. Click **Create database**
4. Click **lab-db** link to view its details
5. Wait until the status changes to **Modifying** or **Available**
   (approximately 4 minutes)
6. Under the **Connectivity & security** tab, copy the **Endpoint** value

The endpoint will look similar to:
```
lab-db.cggq8lhnxvnv.us-west-2.rds.amazonaws.com
```

> Save this endpoint in a text editor — you will need it in the next task.

---

## Task 4: Interact with Your Database

1. Select **AWS Details** above these instructions and copy the **WebServer IP address**
2. Open a new browser tab, paste the IP address, and press **Enter**
3. The web application will display information about the EC2 instance
4. At the top of the page, click the **RDS** link

### Configure the Application to Connect to RDS

Fill in the following fields:

| Field      | Value                              |
|------------|------------------------------------|
| Endpoint   | *(paste the endpoint copied earlier)* |
| Database   | `lab`                              |
| Username   | `main`                             |
| Password   | `lab-password`                     |

5. Click **Submit**

The application will run a command to populate the database. After a few seconds,
an **Address Book** application will appear.

### Test the Application

- Add a new contact
- Edit an existing contact
- Remove a contact

> All data is persisted in the RDS database and automatically replicated
> to the standby instance in the second Availability Zone.

---

## Summary of AWS Resources Created

| Resource              | Name / Value              | Purpose                                  |
|-----------------------|---------------------------|------------------------------------------|
| Security Group        | `DB Security Group`       | Controls inbound access to RDS on port 3306 |
| DB Subnet Group       | `DB Subnet Group`         | Defines subnets available for RDS        |
| RDS DB Instance       | `lab-db` (MySQL)          | Managed relational database              |
| Deployment Type       | Multi-AZ                  | High availability with standby replica   |
| Initial Database      | `lab`                     | Application database                     |
| VPC                   | `Lab VPC`                 | Network environment for all resources    |

---

## Key Concepts

- **Multi-AZ deployment** — RDS automatically provisions a primary and a standby
  instance in separate Availability Zones, providing automatic failover
- **DB Subnet Group** — required by RDS to know which subnets to use; must span
  at least two Availability Zones
- **Security Groups** — act as virtual firewalls; the DB security group only
  accepts traffic from the web server's security group on port 3306
- **Private Subnets** — the RDS instance is placed in private subnets with no
  public access, following security best practices

---

## Additional Resources

- [Amazon RDS Documentation](https://docs.aws.amazon.com/rds/)
- [Amazon RDS FAQs](https://aws.amazon.com/rds/faqs/)
- [Multi-AZ Deployments](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.MultiAZ.html)
- [DB Subnet Groups](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_VPC.WorkingWithRDSInstanceinaVPC.html)
- [VPC Security Groups for RDS](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Overview.RDSSecurityGroups.html)
- [AWS Training and Certification](https://aws.amazon.com/training/)
