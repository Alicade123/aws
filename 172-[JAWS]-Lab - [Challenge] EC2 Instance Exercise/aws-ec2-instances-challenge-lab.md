# AWS EC2 Instances — Challenge Lab Runbook

## Purpose

Reusable guide for completing the AWS EC2 Challenge Lab while understanding the AWS networking and EC2 concepts behind each step.

---

## 1. Lab Objective

Build a simple web application on an Amazon Linux EC2 instance and make it accessible through:

```text
http://<EC2-PUBLIC-IP>/projects.html
```

The page should display:

```text
Alicade's re/Start Project Work
EC2 Instance Challenge Lab
```

---

## 2. Final Architecture

```text
Internet
   |
   v
Internet Gateway
   |
   v
Route Table
0.0.0.0/0 -> IGW
   |
   v
Public Subnet
10.0.1.0/24
   |
   v
Security Group
SSH :22
HTTP :80
   |
   v
EC2 / Amazon Linux / t3.micro
   |
   v
Apache (httpd)
   |
   v
/var/www/html/projects.html
```

---

# Phase 1 — Launch the Lab

1. Choose **Start Lab**.
2. Wait for:

```text
Lab status: ready
```

3. Close the panel.
4. Choose **AWS** to open the AWS Management Console.
5. Confirm the Region.

Example:

```text
us-west-2
```

Use the same Region for all resources.

### Expected result

You are inside the temporary AWS account and know your Region.

---

# Phase 2 — Create the VPC

Navigate:

```text
AWS Console
-> VPC
-> Your VPCs
-> Create VPC
```

Choose:

```text
Resources to create: VPC only
Name: EC2-Challenge-VPC
IPv4 CIDR: 10.0.0.0/16
IPv6: No IPv6 CIDR block
Tenancy: Default
```

Create the VPC.

### Concept

A VPC is your isolated virtual network in AWS.

### Expected result

```text
EC2-Challenge-VPC
10.0.0.0/16
```

---

# Phase 3 — Create the Public Subnet

Navigate:

```text
VPC
-> Subnets
-> Create subnet
```

Configure:

```text
VPC: EC2-Challenge-VPC
Subnet name: EC2-Challenge-Public-Subnet
Availability Zone: Choose an AZ in your Region
IPv4 subnet CIDR: 10.0.1.0/24
```

Create it.

### Expected result

```text
VPC 10.0.0.0/16
|
+-- Public Subnet 10.0.1.0/24
```

### Concept

A subnet is a smaller network inside the VPC.

---

# Phase 4 — Create the Internet Gateway

Navigate:

```text
VPC
-> Internet gateways
-> Create internet gateway
```

Use:

```text
Name: EC2-Challenge-IGW
```

Create it.

Then:

```text
Actions
-> Attach to a VPC
-> EC2-Challenge-VPC
```

Attach it.

### Expected result

```text
EC2-Challenge-VPC
        |
        v
EC2-Challenge-IGW
```

### Concept

The Internet Gateway provides a path between the VPC and the Internet.

---

# Phase 5 — Create and Configure the Route Table

Navigate:

```text
VPC
-> Route tables
-> Create route table
```

Configure:

```text
Name: EC2-Challenge-Public-RT
VPC: EC2-Challenge-VPC
```

Create it.

## Add the Internet route

Select the route table:

```text
Routes
-> Edit routes
-> Add route
```

Configure:

```text
Destination: 0.0.0.0/0
Target: Internet Gateway
Target: EC2-Challenge-IGW
```

Save.

## Associate the subnet

Go to:

```text
Subnet associations
-> Edit subnet associations
```

Select:

```text
EC2-Challenge-Public-Subnet
```

Save.

### Expected result

```text
Public Subnet
      |
      v
Public Route Table
      |
      +-- 10.0.0.0/16 -> local
      |
      +-- 0.0.0.0/0 -> Internet Gateway
```

### Concept

The route table determines where network traffic goes.

A subnet is effectively public when its route table has a route to an Internet Gateway.

---

# Phase 6 — Create the Security Group

Navigate:

```text
EC2
-> Security Groups
-> Create security group
```

Configure:

```text
Name: EC2-Challenge-SG
Description: Security group for EC2 challenge web server
VPC: EC2-Challenge-VPC
```

## Inbound rules

### SSH

Because the lab uses browser-based EC2 Instance Connect:

```text
Type: SSH
Protocol: TCP
Port: 22
Source: EC2 Instance Connect prefix list
```

For `us-west-2`, this appears as the regional EC2 Instance Connect prefix list, such as:

```text
com.amazonaws.us-west-2.ec2-instance-connect
```

or its `pl-...` identifier.

### HTTP

```text
Type: HTTP
Protocol: TCP
Port: 80
Source: 0.0.0.0/0
```

Leave the default outbound rule unless the lab says otherwise.

### Final inbound configuration

```text
SSH   TCP 22   EC2 Instance Connect prefix list
HTTP  TCP 80   0.0.0.0/0
```

### Important troubleshooting lesson

If an existing SSH rule is:

```text
SSH TCP 22 YOUR_PUBLIC_IP/32
```

and you try to change it directly to a prefix list, AWS may show:

```text
You may not specify a prefix list for an existing IPv4 CIDR rule.
```

Fix:

1. Delete the existing SSH CIDR rule.
2. Add a new SSH rule.
3. Select the EC2 Instance Connect prefix list.
4. Save.

Do not delete the HTTP rule.

### Concept

A Security Group acts as a virtual firewall for the EC2 instance.

---

# Phase 7 — Launch the EC2 Instance

Navigate:

```text
EC2
-> Instances
-> Launch instance
```

## Name

```text
EC2-Challenge-WebServer
```

## AMI

Choose:

```text
Amazon Linux
```

Amazon Linux 2023 is suitable if provided by the lab.

## Instance type

The challenge requires:

```text
T3
Smaller than medium
```

Example:

```text
t3.micro
```

## Key pair

The challenge uses browser-based EC2 Instance Connect. Follow the lab's supported Instance Connect workflow.

---

# Phase 8 — Configure Network Settings

Expand:

```text
Network settings
```

Use:

```text
VPC: EC2-Challenge-VPC
Subnet: EC2-Challenge-Public-Subnet
Auto-assign Public IP: Enable
Security group: EC2-Challenge-SG
```

The instance should eventually have:

```text
Private IPv4: 10.0.1.x
Public IPv4: <public-ip>
```

### Expected result

The instance is placed in the intended public subnet and receives a public IPv4 address.

---

# Phase 9 — Configure Storage

The challenge requires General Purpose SSD `gp2`.

Under the root volume:

```text
Volume type: gp2
```

Use other lab defaults unless instructed otherwise.

---

# Phase 10 — Configure User Data

Use this for Amazon Linux versions that use `dnf`:

```bash
#!/bin/bash

dnf install -y httpd

systemctl enable httpd
systemctl start httpd

chmod 777 /var/www/html
```

If the image uses `yum`:

```bash
#!/bin/bash

yum install -y httpd

systemctl enable httpd
systemctl start httpd

chmod 777 /var/www/html
```

## What the commands do

```bash
dnf install -y httpd
```

Installs Apache.

```bash
systemctl enable httpd
```

Makes Apache start automatically during boot.

```bash
systemctl start httpd
```

Starts Apache immediately.

```bash
chmod 777 /var/www/html
```

Provides the write permission requested by the challenge.

---

# Phase 11 — Launch and Wait

Before launching, verify:

```text
Name: EC2-Challenge-WebServer
AMI: Amazon Linux
Instance type: t3.micro
VPC: EC2-Challenge-VPC
Subnet: EC2-Challenge-Public-Subnet
Public IPv4: Enabled
Security group: EC2-Challenge-SG
Root volume: gp2
User data: httpd installation script
```

Launch.

Then wait for:

```text
Instance state: Running
Status checks: 2/2 checks passed
```

Allow time for user data to finish.

---

# Phase 12 — Capture System Log Evidence

The challenge requires a screenshot showing successful `httpd` installation.

Navigate to the instance:

```text
Actions
-> Monitor and troubleshoot
-> Get system log
```

Look for evidence related to user-data execution and `httpd`.

Capture the screenshot required by the instructor.

### Note

User-data/cloud-init output may not all appear in the EC2 system log. Additional verification can be done from inside the instance using cloud-init logs.

---

# Phase 13 — Connect Using EC2 Instance Connect

Navigate:

```text
EC2
-> Instances
-> Select EC2-Challenge-WebServer
-> Connect
-> EC2 Instance Connect
```

Username for Amazon Linux:

```text
ec2-user
```

Connect.

### Expected result

A browser-based terminal opens:

```text
[ec2-user@ip-10-0-1-xxx ~]$
```

---

# Phase 14 — Verify Apache

Run:

```bash
sudo systemctl status httpd
```

Expected:

```text
Active: active (running)
```

If necessary:

```bash
sudo systemctl start httpd
sudo systemctl enable httpd
```

Verify the version:

```bash
httpd -v
```

---

# Phase 15 — Create `projects.html`

Run:

```bash
nano projects.html
```

Use:

```html
<!DOCTYPE html>
<html>
<body>
<h1>Alicade's re/Start Project Work</h1>
<p>EC2 Instance Challenge Lab</p>
</body>
</html>
```

Replace `Alicade` with your own name if required.

Save:

```text
Ctrl + O
Enter
Ctrl + X
```

---

# Phase 16 — Copy the Page to Apache

Run:

```bash
sudo cp projects.html /var/www/html/projects.html
```

Verify:

```bash
ls -l /var/www/html/projects.html
```

Expected:

```text
projects.html
```

---

# Phase 17 — Test Locally

Run:

```bash
curl http://localhost/projects.html
```

Expected:

```html
<!DOCTYPE html>
<html>
<body>
<h1>Alicade's re/Start Project Work</h1>
<p>EC2 Instance Challenge Lab</p>
</body>
</html>
```

This proves that Apache and the file work locally on the EC2 instance.

---

# Phase 18 — Test from the Browser

Copy the EC2 instance's Public IPv4 address.

Example:

```text
44.252.14.7
```

Open:

```text
http://44.252.14.7/projects.html
```

Use HTTP, not HTTPS, because HTTPS was not configured.

### Expected result

The browser displays:

```text
Alicade's re/Start Project Work

EC2 Instance Challenge Lab
```

Capture the required webpage screenshot.

---

# Phase 19 — Troubleshooting

## EC2 Instance Connect fails

Check:

```text
1. Public IPv4 exists
2. Security Group TCP 22 allows EC2 Instance Connect prefix list
3. Route table has 0.0.0.0/0 -> Internet Gateway
4. Public subnet is associated with that route table
5. Internet Gateway is attached to the VPC
6. Instance status checks are 2/2
7. Username is ec2-user
```

## Web page fails

First test:

```bash
curl http://localhost/projects.html
```

### If localhost fails

Check:

```bash
sudo systemctl status httpd
ls -l /var/www/html/projects.html
```

### If localhost works but browser fails

Check:

```text
Public IPv4
Security Group TCP 80
Route Table
Internet Gateway
Public Subnet
```

Security Group should contain:

```text
HTTP TCP 80 0.0.0.0/0
```

### If browser returns 404

Check:

```bash
ls -l /var/www/html/projects.html
curl http://localhost/projects.html
```

Verify URL:

```text
http://PUBLIC-IP/projects.html
```

---

# Phase 20 — QA Test Cases

| ID | Test | Expected Result |
|---|---|---|
| EC2-01 | VPC exists | VPC created |
| EC2-02 | Subnet exists | Subnet in correct VPC |
| EC2-03 | Internet Gateway attached | IGW attached |
| EC2-04 | Route table | `0.0.0.0/0 -> IGW` |
| EC2-05 | Subnet association | Public subnet associated |
| EC2-06 | SSH rule | TCP 22 allowed |
| EC2-07 | HTTP rule | TCP 80 allowed |
| EC2-08 | EC2 launches | Running |
| EC2-09 | Status checks | 2/2 passed |
| EC2-10 | Public IPv4 | Address exists |
| EC2-11 | Instance Connect | SSH succeeds |
| EC2-12 | httpd installed | `httpd -v` works |
| EC2-13 | httpd running | Active/running |
| EC2-14 | HTML file exists | `projects.html` found |
| EC2-15 | Local HTTP | `curl localhost` succeeds |
| EC2-16 | External HTTP | Browser loads page |
| EC2-17 | Wrong page | HTTP 404 |
| EC2-18 | Remove HTTP rule | External access fails |

---

# Phase 21 — Evidence Checklist

## Screenshot 1 — System Log

Show evidence related to successful `httpd` installation/startup.

```text
[ ] Screenshot captured
```

## Screenshot 2 — Working Web Page

Show:

```text
http://<PUBLIC-IP>/projects.html
```

with:

```text
Alicade's re/Start Project Work
EC2 Instance Challenge Lab
```

```text
[ ] Screenshot captured
```

---

# Phase 22 — Final Infrastructure Checklist

## VPC

```text
[ ] VPC created
[ ] 10.0.0.0/16
[ ] Correct Region
```

## Subnet

```text
[ ] Subnet created
[ ] Correct VPC
[ ] 10.0.1.0/24
```

## Internet

```text
[ ] Internet Gateway created
[ ] IGW attached to VPC
[ ] Route table created
[ ] 0.0.0.0/0 -> IGW
[ ] Public subnet associated
```

## Security

```text
[ ] Security Group created
[ ] SSH TCP 22 from EC2 Instance Connect prefix list
[ ] HTTP TCP 80 from 0.0.0.0/0
```

## EC2

```text
[ ] Amazon Linux
[ ] T3 instance
[ ] Smaller than medium
[ ] Public IPv4 enabled
[ ] gp2 root volume
[ ] User data configured
[ ] Running
[ ] 2/2 checks passed
```

## Web Server

```text
[ ] httpd installed
[ ] httpd running
[ ] /var/www/html available
[ ] projects.html created
[ ] projects.html copied
[ ] curl localhost works
[ ] Browser access works
```

## Submission

```text
[ ] System log screenshot
[ ] Working webpage screenshot
[ ] Evidence submitted
```

---

# Phase 23 — Key AWS Concepts

### VPC

Your isolated virtual network.

### Subnet

A smaller network inside a VPC.

### Internet Gateway

Provides a path between the VPC and Internet.

### Route Table

Determines where network traffic goes.

### Public Subnet

A subnet whose route table provides a route to an Internet Gateway.

### Public IPv4

The public address used for Internet communication.

### Security Group

A virtual firewall controlling allowed traffic.

### Port 22

SSH. Used by EC2 Instance Connect.

### Port 80

HTTP. Used by the browser to reach Apache.

### EC2

A virtual server.

### Amazon Linux

The operating system on the EC2 instance.

### Apache/httpd

The web server serving files from:

```text
/var/www/html
```

### User Data

A startup script used to automatically configure an EC2 instance.

---

# Phase 24 — Troubleshooting Mental Model

For an external web request:

```text
Browser
   |
   | HTTP :80
   v
Internet
   |
   v
Internet Gateway
   |
   v
Route Table
   |
   v
Public Subnet
   |
   v
Security Group
   |
   | TCP 80 allowed
   v
EC2
   |
   v
Apache/httpd
   |
   v
/var/www/html/projects.html
```

For EC2 Instance Connect:

```text
Browser
   |
   v
AWS EC2 Console
   |
   v
EC2 Instance Connect
   |
   | TCP 22
   v
Security Group
   |
   v
EC2
   |
   v
SSH
```

If:

```text
curl http://localhost/projects.html
```

works but the browser fails, Apache is probably fine. Investigate:

```text
Public IP
Security Group
Route Table
Internet Gateway
Subnet
```

---

# Phase 25 — Final Learning Questions

You should be able to answer these without looking at the guide:

### Why do we need a VPC?

To provide an isolated virtual network for AWS resources.

### Why do we need a subnet?

To place the EC2 instance inside a specific network segment.

### Why do we need an Internet Gateway?

To provide connectivity between the VPC and Internet.

### Why do we need a route table?

To determine where network traffic goes.

### Why is the subnet public?

Because its route table has:

```text
0.0.0.0/0 -> Internet Gateway
```

### Why does EC2 need a public IPv4?

To communicate with the Internet using a public address.

### Why do we need TCP 22?

For SSH and EC2 Instance Connect.

### Why do we need TCP 80?

For HTTP web traffic.

### Why use user data?

To automatically install and configure the web server during startup.

### Why can localhost work while the browser fails?

Because Apache can be working while external networking is misconfigured.

---

# Quick Rebuild Summary

```text
1. Start Lab
2. Choose Region
3. Create VPC
4. Create subnet
5. Create Internet Gateway
6. Attach IGW to VPC
7. Create route table
8. Add 0.0.0.0/0 -> IGW
9. Associate subnet
10. Create Security Group
11. Allow SSH :22 from EC2 Instance Connect prefix list
12. Allow HTTP :80 from 0.0.0.0/0
13. Launch Amazon Linux t3.micro
14. Select VPC + public subnet
15. Enable public IPv4
16. Select Security Group
17. Select gp2 root volume
18. Add httpd user data
19. Launch
20. Wait for 2/2 checks
21. EC2 Instance Connect
22. Verify httpd
23. Create projects.html
24. Copy to /var/www/html
25. curl localhost
26. Open http://PUBLIC-IP/projects.html
27. Capture screenshots
28. Submit evidence
29. End Lab
```

---

# Lab Completion

After all requirements and screenshots are complete:

```text
AWS Training Lab
-> End Lab
-> Yes
```

The temporary lab resources will be terminated.
