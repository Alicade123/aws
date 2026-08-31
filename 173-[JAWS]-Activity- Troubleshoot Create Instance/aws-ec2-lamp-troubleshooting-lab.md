# AWS Activity — Troubleshooting the Creation of an EC2 Instance

## Overview

This lab uses the **AWS CLI** to launch an Amazon Linux EC2 instance and troubleshoot intentional configuration errors.

The final instance runs a **LAMP stack**:

- Linux / Amazon Linux
- Apache (`httpd`)
- MariaDB
- PHP
- Café Web Application

---

## 1. Lab Objectives

By completing this activity, you should be able to:

- Launch an EC2 instance using the AWS CLI.
- Configure and use the AWS CLI.
- Understand Region-specific EC2 resources.
- Troubleshoot `RunInstances` errors.
- Troubleshoot Security Group/networking problems.
- Use `nmap` to inspect accessible ports.
- Understand EC2 user data and `cloud-init`.
- Verify a LAMP application backed by MariaDB.

---

## 2. Lab Environment

After choosing **Start Lab**:

1. Wait for `Lab status: ready`.
2. Open the AWS Management Console using **AWS**.
3. Do **not** change the lab Region unless instructed.

Example lab Region used:

```text
us-west-2
```

---

## 3. Task 1 — Connect to the CLI Host

Navigate:

```text
AWS Console
→ EC2
→ Instances
→ CLI Host
→ Connect
→ EC2 Instance Connect
→ Connect
```

Expected:

```text
[ec2-user@cli-host ~]$
```

The CLI Host is used to execute the AWS CLI commands.

---

## 4. Task 2 — Configure AWS CLI

Run:

```bash
aws configure
```

Enter the temporary credentials supplied by the lab:

```text
AWS Access Key ID: <AccessKey from lab>
AWS Secret Access Key: <SecretKey from lab>
Default region name: <LabRegion>
Default output format: json
```

Verify:

```bash
aws configure list
aws configure get region
```

Expected example:

```text
us-west-2
```

> **Security:** Never commit AWS credentials to GitHub. Training credentials should only be used for the lab and should not be exposed in documentation.

---

## 5. Task 3 — Prepare the Scripts

Navigate:

```bash
cd ~/sysops-activity-files/starters
```

List files:

```bash
ls -l
```

Expected files:

```text
create-lamp-instance-v2.sh
create-lamp-instance-userdata-v2.txt
```

Create a backup:

```bash
cp create-lamp-instance-v2.sh create-lamp-instance.backup
```

Inspect:

```bash
cat create-lamp-instance-v2.sh
cat create-lamp-instance-userdata-v2.txt
```

---

## 6. What the Shell Script Does

The script discovers:

| Value | Purpose |
|---|---|
| `instanceType` | EC2 instance size, `t3.small` |
| VPC | Finds `Cafe VPC` |
| Region | Region containing the Café VPC |
| Subnet | Finds `Cafe Public Subnet 1` |
| Key pair | Gets an available EC2 key pair |
| AMI | Gets the Amazon Linux 2 AMI |
| Security Group | Creates `cafeSG` |
| Instance | Creates `cafeserver` |

The user-data file installs and configures:

```text
Apache/httpd
MariaDB
PHP
Café Web Application
```

---

# 7. Run the Script

Run:

```bash
./create-lamp-instance-v2.sh
```

The script is intentionally broken.

The challenge is to identify and fix the problems.

---

# 8. Issue #1 — Wrong EC2 Region

## Symptom

You receive:

```text
InvalidAMIID.NotFound
```

Example:

```text
An error occurred (InvalidAMIID.NotFound) when calling the RunInstances operation:
The image id '[ami-xxxxxxxxxx]' does not exist
```

## Investigate

Find the command:

```bash
grep -n "run-instances" create-lamp-instance-v2.sh
```

Inspect it:

```bash
sed -n '145,180p' create-lamp-instance-v2.sh
```

The broken script contains:

```bash
--region us-east-1
```

But earlier it discovered the correct Region, for example:

```text
Region: us-west-2
```

### Why this causes the error

AMI IDs are Region-specific.

The script found an AMI in `us-west-2` but attempted to launch it in `us-east-1`.

## Verify the AMI

```bash
aws ec2 describe-images   --image-ids <AMI-ID>
```

If the AMI exists in the configured Region, its details will be returned.

## Fix

Change:

```bash
--region us-east-1
```

to:

```bash
--region $region
```

Quick command:

```bash
sed -i 's/--region us-east-1/--region $region/' create-lamp-instance-v2.sh
```

Verify:

```bash
sed -n '154,170p' create-lamp-instance-v2.sh
```

Expected:

```bash
--region $region ```

Run again:

```bash
./create-lamp-instance-v2.sh
```

If prompted to delete resources created by the previous failed attempt, answer:

```text
Y
```

### Expected result

The EC2 instance launches successfully and eventually displays:

```text
The public IP of your LAMP instance is: <PUBLIC-IP>
```

---

# 9. Issue #2 — HTTP Port 8080 Instead of 80

After Issue #1 is fixed, the EC2 instance launches, but:

```text
http://<PUBLIC-IP>
```

does not load.

Inspect the Security Group portion:

```bash
sed -n '140,155p' create-lamp-instance-v2.sh
```

The script says:

```bash
echo "Opening port 80 in the new security group"
```

but the actual command contains:

```bash
--port 8080
```

Therefore:

```text
TCP 22   → SSH
TCP 8080 → allowed
TCP 80   → blocked
```

Apache uses:

```text
TCP 80
```

---

## 10. Confirm with nmap

Install `nmap` on the CLI Host:

```bash
sudo yum install -y nmap
```

Scan:

```bash
nmap -Pn <PUBLIC-IP>
```

The important observation is that TCP 80 is not accessible.

---

## 11. Fix Issue #2

Change:

```bash
--port 8080
```

to:

```bash
--port 80
```

Quick command:

```bash
sed -i 's/--port 8080/--port 80/' create-lamp-instance-v2.sh
```

Verify:

```bash
grep -n -A5 "Opening port 80" create-lamp-instance-v2.sh
```

Expected:

```bash
--port 80 ```

---

## 12. Recreate the Resources

Run:

```bash
./create-lamp-instance-v2.sh
```

If prompted to delete an existing `cafeserver` instance or `cafeSG`, answer:

```text
Y
```

The new Security Group should allow:

```text
TCP 22 → SSH
TCP 80 → HTTP
```

---

# 13. Verify Port 80

After the instance receives a public IP:

```bash
nmap -Pn <PUBLIC-IP>
```

Expected:

```text
22/tcp   open   ssh
80/tcp   open   http
```

This confirms the Security Group permits HTTP traffic.

---

# 14. Connect to the LAMP Instance

Navigate:

```text
AWS Console
→ EC2
→ Instances
→ cafeserver
→ Connect
→ EC2 Instance Connect
→ Connect
```

---

# 15. Verify Apache

Run:

```bash
sudo systemctl status httpd
```

Expected:

```text
Active: active (running)
```

Test locally:

```bash
curl http://localhost
```

Expected initial test:

```text
Hello From Your Web Server!
```

---

# 16. Verify EC2 User Data

Amazon Linux uses `cloud-init` to execute EC2 user data.

Run:

```bash
sudo tail -f /var/log/cloud-init-output.log
```

Look for successful messages involving:

```text
Apache/httpd
MariaDB
PHP
Café Web Application
Create Database script completed
```

Stop:

```text
Ctrl+C
```

Entire log:

```bash
sudo cat /var/log/cloud-init-output.log
```

---

# 17. LAMP Architecture

```text
Internet
   |
   | HTTP :80
   v
EC2 Instance
   |
   +--> Apache/httpd
           |
           +--> PHP
                   |
                   +--> MariaDB
                           |
                           +--> Café Database
```

| Component | Purpose |
|---|---|
| EC2 | Virtual server |
| Amazon Linux | Operating system |
| Apache/httpd | Web server |
| PHP | Application/backend language |
| MariaDB | Relational database |
| Security Group | Network firewall |
| User Data | Startup automation |
| cloud-init | Executes startup configuration |

---

# 18. Verify the Café Application

Open:

```text
http://<PUBLIC-IP>/cafe/
```

Expected:

```text
Café Web Application home page
```

Example:

```text
http://54.187.59.84/cafe/
```

---

# 19. Test Database Functionality

On the Café website:

```text
Menu
→ Select several items
→ Submit Order
```

Verify the Order Confirmation page.

Place another order with different items.

Then:

```text
Order History
```

Expected:

- Both orders appear.
- Order details are retained.

This confirms that the web application can communicate with MariaDB.

---

# 20. Final Verification Checklist

```text
[✓] CLI Host connected
[✓] AWS CLI configured
[✓] Correct Region identified
[✓] Starter script backed up
[✓] Shell script inspected
[✓] User-data script inspected
[✓] Issue #1 identified
[✓] Wrong EC2 Region corrected
[✓] EC2 instance launched
[✓] Issue #2 identified
[✓] Port 8080 corrected to port 80
[✓] nmap used
[✓] TCP 22 accessible
[✓] TCP 80 accessible
[✓] Apache/httpd running
[✓] User data executed
[✓] MariaDB configured
[✓] PHP configured
[✓] Café website accessible
[✓] Orders submitted
[✓] Order History verified
```

---

# 21. Troubleshooting Mental Model

When a website does not work, troubleshoot from outside inward:

```text
Public IP exists?
       ↓
Security Group allows TCP 80?
       ↓
Network path works?
       ↓
Apache running?
       ↓
curl localhost works?
       ↓
User data completed?
       ↓
PHP works?
       ↓
MariaDB works?
       ↓
Application works?
```

This layered approach is useful for AWS, Linux, DevOps, and QA troubleshooting.

---

# 22. Useful Commands

### AWS CLI

```bash
aws configure
aws configure list
aws configure get region
```

### EC2

```bash
aws ec2 describe-instances
aws ec2 describe-images --image-ids <AMI-ID>
aws ec2 describe-subnets
aws ec2 describe-security-groups
```

### Script inspection

```bash
cat create-lamp-instance-v2.sh
grep -n "run-instances" create-lamp-instance-v2.sh
sed -n '145,180p' create-lamp-instance-v2.sh
```

### Networking

```bash
nmap -Pn <PUBLIC-IP>
curl http://localhost
```

### Apache

```bash
sudo systemctl status httpd
sudo systemctl start httpd
sudo systemctl restart httpd
```

### User data logs

```bash
sudo tail -f /var/log/cloud-init-output.log
sudo cat /var/log/cloud-init-output.log
```

---

# 23. Key Lessons

### 1. AWS resources can be Region-specific

Always compare:

```text
AMI Region
VPC Region
Subnet Region
EC2 launch Region
```

### 2. `InvalidAMIID.NotFound` does not automatically mean the AMI ID is wrong

Check whether the AMI exists in the Region being used.

### 3. Security Groups control network access

Typical web server:

```text
HTTP → TCP 80
```

Typical SSH:

```text
SSH → TCP 22
```

Opening TCP 8080 does not open TCP 80.

### 4. User data automates server initialization

EC2 user data can install packages, configure services, deploy files, and initialize applications automatically during startup.

### 5. `nmap` helps distinguish application problems from network problems

If port 80 is closed, investigate the Security Group/network path before troubleshooting Apache.

---

# 24. Lab Completion

After the Café application and Order History work correctly:

1. Capture required screenshots.
2. End the lab.
3. Choose **End Lab**.
4. Confirm **Yes**.
5. Wait for resource termination.

---

# 25. Final Mental Model

```text
                    AWS Region
                       |
                       v
                  VPC / Subnet
                       |
                       v
                Security Group
                 /                        TCP 22          TCP 80
               |               |
              SSH             HTTP
               |               |
               +------ EC2 ----+
                       |
                  Amazon Linux
                       |
                    Apache
                       |
                     PHP
                       |
                   MariaDB
                       |
                 Café Database
                       |
                 Café Web App
```

The two main troubleshooting concepts in this exercise are:

```text
1. Is the EC2 resource being created in the correct AWS Region?
2. Is the network path allowing traffic to the application port?
```

These concepts are reusable across many AWS troubleshooting scenarios.
