# AWS Academy Lab: Troubleshooting a VPC

## Overview

This lab focuses on troubleshooting Amazon VPC networking issues and analyzing VPC Flow Logs.

### Key Objectives

- Create and configure VPC Flow Logs.
- Troubleshoot connectivity issues in a VPC.
- Analyze flow log data to identify network problems.
- Understand the interaction between:
  - VPCs
  - Subnets
  - Route Tables
  - Internet Gateways
  - Security Groups
  - Network ACLs
  - EC2 Instances

---

# Environment

The lab environment contains:

- VPC1
  - Public Subnet
  - Private Subnet
  - Café Web Server EC2 Instance
  - Internet Gateway
  - Route Tables
- VPC2
  - CLI Host EC2 Instance
- VPC Flow Logs
- Amazon S3 Bucket (Flow Log Storage)

---

# Task 1: Connect to CLI Host

1. Open **EC2 Console**
2. Select **CLI Host**
3. Click **Connect**
4. Use **EC2 Instance Connect**

Configure AWS CLI:

```bash
aws configure
```

Example:

```text
AWS Access Key ID: <AccessKey>
AWS Secret Access Key: <SecretKey>
Default region name: us-west-2
Default output format: json
```

---

# Task 2: Create VPC Flow Logs

## Step 1: Create an S3 Bucket

```bash
aws s3api create-bucket \
--bucket flowlog###### \
--region us-west-2 \
--create-bucket-configuration LocationConstraint=us-west-2
```

Example output:

```json
{
  "Location": "http://flowlog098743.s3.amazonaws.com/"
}
```

---

## Step 2: Get VPC1 ID

```bash
aws ec2 describe-vpcs \
--query 'Vpcs[*].[VpcId,Tags[?Key==`Name`].Value,CidrBlock]' \
--filters "Name=tag:*ame,Values='VPC1'"
```

Example:

*``text*vpc-035*b19dfafdec5db
```

---

## Step 3:*Create Flow Logs

```bash
aws ec2 *reate-flow-logs \
--resource-type *PC \
--resource*ids <v*c-id> \
--traffic-type ALL \
--log*destination-type s3 \
--log-destin*tion arn:aws:s3:::<bucket-name>
``*

---

## Step 4: Verify*Flow Logs

```bash
aws ec2 describ*-flow-logs
``*

*xpected:

```text
FlowLogStatus: A*TIVE
LogDestinationType: s3
Delive*LogsStatus: SUCCESS
```

---

# Ta*k 3: Troubleshooting VPC Connectiv*ty

---

# Challenge #1: Web Serve* Not Reachable

## Symptoms

* Web page does not load.
- Browser*times out.
- EC2 Instance Connect *ails.

---

## Verify Instance Sta*us

```bash
*ws ec2 describe-instances \
--filt*r "Name=ip-address,Values='<WebSer*erIP>'"
```

Important checks:

- *nstance is Running
- Public IP exi*ts
- Correct subnet
- Correct secu*ity group attached

---

## Check *ecurity Group

```bash
*ws ec2 describe-security-groups \
*-group-ids*<WebServerSgId>
```

Expected*inbound rules:

```text*TCP 80 ->*0.0.0.0/0
TCP *2 ->*0.0.0.0/0
```

In*this lab:

 Security Group was co*rect.

---

##*Check Route Table

```bash*aws ec2 describe-route-tables*\
--route-table-ids <RouteTableID>*```

### What We Found

Public rou*e table:

```text*10.0.0.0/16 -> local
*``

### Missing Route

```text
0.0*0.0/0 -> Internet Gateway
```

*ithout this route:

- Public subne* is not truly public.
- Internet*traffic cannot reach*the EC2 instance.
- HTTP and SSH f*il.

---

## Fix

```bash*aws*ec2 create-route \
--route-table-i* rtb-0c7ead3b5405db10e \
--destina*ion-cidr-block 0.*.0.0/0 \
--gateway-id igw-095*57fc75379ddd5
```

Verify:

```bas*
aws*ec2 describe-route-tables \
--rout*-table-ids rtb-0c7ead3b5405db10e
`*`

Expected*

```text
10.0.0.0*16 -> local
0.*.0.0/0 ->*igw-095857fc75379ddd5
```

*--

##*Root Cause

**Missing default rout* to the Internet Gateway in the pu*lic route table.**

---

## Result*
Refreshing:

```text
http*//<WebServerIP>
```

Display:

```*ext
Hello From Your Web Server!
``*

 Challenge*#1 solved

---

# Challenge #2* SSH Access Fails

## Symptoms

Ev*n*after*the website becomes accessible:

-*EC2 Instance Connect still fails.
* SSH cannot reach the server.

---*
## Check Network ACL

```bash
aws*ec2 describe-network-acls \
--filt*r "Name=association.subnet-id,Valu*s='<PublicSubnetID>'" \
--query*'Network*cls[*].[NetworkAclId,Entries]'
```*
Review:

- Inbound rules
- Out*ound*rules
- Rule numbers
- ALLOW*/ DENY actions

---

*# Common*Problem

A Network*ACL contains a DENY rule blocking:*
```text
TCP Port 22
```

*r*
```text
Ephemeral return ports
``*

---

## Fix

Delete the problema*ic NACL rule.

Example:

```bash
a*s ec2 delete-network-acl-entry \
-*network-acl-id <acl-id> \
--rule-n*mber <rule-number> \
--egress
```
*or

```bash
aws ec2 delete-network*acl-entry \
--network-acl-id <acl-*d> \
--rule-number <rule-number>
`*`

Depending on whether the bad ru*e is outbound or inbound.

---

##*Verify

Use EC2 Instance Connect a*ain.

Expected:

```text
Connected*Successfully
```

Confirm:

```bas*
hostname
```

Expected result:

`*`text
web-server
```

 Challenge *2 solved

---

# Task 4: Analyze V*C Flow Logs

---

## Download Logs*
Create a directory:

```bash
mkdi* flowlogs
cd flowlogs
```

---

##*List Buckets

```bash
aws s3 ls
``*

---

## Download Flow Logs

```b*sh
aws s3 cp s3://<flowlog-bucket>* . --recursive
```

---

## Naviga*e to Log Directory

```bash
cd AWS*ogs/<AccountID>/vpcflow*ogs/us-west-2/yyyy/mm/dd/
```

---*
## Extract Files

```bash
gunzip *.gz
```

---

## View Logs

```*ash
ls
```

```bash
head <filename*
```

Example log structure:

```t*xt
version account-id interface-id*srcaddr dstaddr srcport dstport pr*tocol packets bytes start end acti*n log-status
```

---

# Searching*the Logs

## Find Rejected Traffic*
```bash
grep -rn REJECT .
```

--*

## Count Rejected Records

```ba*h
grep -rn REJECT . | wc -l
```

-*-

## Find Rejected SSH Traffic

`*`bash
grep -rn 22 . | grep REJECT
*``

This shows rejected SSH attemp*s.

---

## Filter by Your Public *P

```bash
grep -rn 22 . | grep RE*ECT | grep <your-public-ip>
```

R*sult:

```text
Failed SSH connecti*n attempts
```

---

# Verify Netw*rk Interface

```bash
aws ec2 desc*ibe-network-interfaces \
--filters*"Name=association.public-ip,Values*'<WebServerIP>'" \
--query 'Networ*Interfaces[*].[NetworkInterfaceId,*ssociation.PublicIp]'
```

Confirm*the ENI in the log matches the Web*Server ENI.

---

# Convert Timest*mps

Example:

```bash
date -d @15*4496931
```

Check current time:

*``bash
date
```

This helps correl*te log entries with troubleshootin* actions.

---

# Key Lessons Lear*ed

## Security Groups

- Stateful*- Allow traffic based on rules
- R*turn traffic automatically allowed*
Typical rules:

```text
22/TCP ->*SSH
80/TCP -> HTTP
```

---

## Ro*te Tables

For a public subnet:

`*`text
Local Route
0.0.0.0/0 -> Int*rnet Gateway
```

Missing the defa*lt route makes the subnet effectiv*ly private.

---

## Network ACLs
*- Stateless
- Evaluated by rule nu*ber
- Can explicitly ALLOW or DENY*traffic
- Must allow both request *nd response traffic

---

## VPC F*ow Logs

Useful for troubleshootin*:

- ACCEPT traffic
- REJECT traff*c
- Source IPs
- Destination IPs
-*Ports
- Timestamps
- Network Inter*aces

---

# Actual Issue Encounte*ed in This Lab

### Problem

The P*blic Route Table was missing:

```*ext
0.0.0.0/0 -> Internet Gateway
*``

### Fix

```bash
aws ec2 creat*-route \
--route-table-id rtb-0c7e*d3b5405db10e \
--destination-cidr-*lock 0.0.0.0/0 \
--gateway-id igw-*95857fc75379ddd5
```

### Result

*``text
Hello From Your Web Server!*```

The web server became publicl* reachable.

---

# Conclusion

Th*s lab demonstrated how to:

- Crea*e VPC Flow Logs
- Troubleshoot Sec*rity Groups
- Troubleshoot Route Tables
- Troubleshoot Network ACLs
- Analyze flow log data
- Restore connectivity to an EC2 web server

A systematic troubleshooting process should always follow:

1. Verify Instance State
2. Verify Security Groups
3. Verify Route Tables
4. Verify Internet Gateway
5. Verify Network ACLs
6. Validate with VPC Flow Logs

Following this sequence dramatically reduces the time required to diagnose VPC networking issues.