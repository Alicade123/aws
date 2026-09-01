# Lab: Automation with AWS CloudFormation

## Overview

AWS CloudFormation enables Infrastructure as Code (IaC) by defining AWS resources in templates that can be deployed, updated, and deleted automatically.

This lab demonstrates how to:

- Deploy infrastructure using CloudFormation
- Modify existing stacks
- Add new resources without redeploying existing infrastructure
- Use Parameters, Resources, Outputs, and References
- Delete an entire environment with a single action

---

## Learning Objectives

By completing this lab, you will be able to:

- Create CloudFormation stacks
- Deploy networking resources such as VPCs and Security Groups
- Add Amazon S3 buckets to existing stacks
- Add Amazon EC2 instances to existing stacks
- Reference resources using `!Ref`
- Use Systems Manager Parameter Store for dynamic AMI selection
- Delete CloudFormation stacks and associated resources

---

# Task 1: Deploy a CloudFormation Stack

## Architecture

The initial template deploys:

- VPC
- Public Subnet
- Security Group
- Supporting networking resources

---

## CloudFormation Template Structure

### Parameters

Used to collect input values.

Example:

```yaml
Parameters:
  VPCCidr:
    Type: String
```

---

### Resources

Defines infrastructure resources.

Example:

```yaml
Resources:
  VPC:
    Type: AWS::EC2::VPC
```

---

### Outputs

Displays selected values after deployment.

Example:

```yaml
Outputs:
  SecurityGroup:
    Value: !Ref AppSecurityGroup
```

---

## Deploy the Stack

1. Open **CloudFormation Console**
2. Click **Create Stack**
3. Select **Upload a template file**
4. Upload `task1.yaml`
5. Click **Next**

Configure:

```text
Stack Name: Lab
```

Leave all CIDR values at their defaults.

Continue through:

```text
Next → Next → Create Stack
```

---

## Verify Deployment

Monitor:

```text
CREATE_IN_PROGRESS
```

until:

```text
CREATE_COMPLETE
```

Review:

- Events Tab
- Resources Tab

CloudFormation automatically creates resources in the correct order based on dependencies.

---

# Task 2: Add an Amazon S3 Bucket

## Objective

Update the existing stack to deploy an S3 bucket.

---

## Template Update

Under the `Resources` section, add:

```yaml
S3Bucket:
  Type: AWS::S3::Bucket
```

No additional properties are required.

---

## Update Stack

1. Select **Lab**
2. Click **Update**
3. Select **Replace current template**
4. Upload modified template
5. Click **Next**
6. Review changes

Expected preview:

```text
Add AWS::S3::Bucket
```

7. Click **Update Stack**

---

## Verify

Wait for:

```text
UPDATE_COMPLETE
```

Open:

```text
Resources
```

Confirm:

```text
AWS::S3::Bucket
```

was created.

CloudFormation automatically generates a globally unique bucket name.

---

## Key Takeaway

CloudFormation only deploys newly added resources during updates.

Existing resources remain unchanged.

---

# Task 3: Add an Amazon EC2 Instance

## Objective

Deploy an EC2 instance inside the existing infrastructure.

---

## Step 1: Add Dynamic AMI Parameter

Under `Parameters`:

```yaml
AmazonLinuxAMIID:
  Type: AWS::SSM::Parameter::Value<AWS::EC2::Image::Id>
  Default: /aws/service/ami-amazon-linux-latest/amzn2-ami-hvm-x86_64-gp2
```

### Why?

Instead of hardcoding an AMI ID:

```text
ami-xxxxxxxx
```

CloudFormation retrieves the latest Amazon Linux 2 AMI automatically from Systems Manager Parameter Store.

Benefits:

- Region independent
- Always current
- Easier maintenance

---

## Step 2: Use Resource References

CloudFormation resources can reference one another.

Example:

```yaml
VpcId: !Ref VPC
```

`!Ref` returns the identifier of another resource.

---

## Step 3: Add EC2 Instance

Under `Resources`:

```yaml
AppServer:
  Type: AWS::EC2::Instance
  Properties:
    ImageId: !Ref AmazonLinuxAMIID
    InstanceType: t3.micro
    SecurityGroupIds:
      - !Ref AppSecurityGroup
    SubnetId: !Ref PublicSubnet
    Tags:
      - Key: Name
        Value: App Server
```

---

## Resource Dependencies

The EC2 instance references:

### AMI

```yaml
!Ref AmazonLinuxAMIID
```

### Security Group

```yaml
!Ref AppSecurityGroup
```

### Subnet

```yaml
!Ref PublicSubnet
```

CloudFormation automatically determines deployment order.

---

## Update Stack

1. Select **Lab**
2. Click **Update**
3. Upload modified template
4. Continue through the wizard
5. Review changes

Expected preview:

```text
Add AWS::EC2::Instance
```

6. Click **Update Stack**

---

## Verify

Wait until:

```text
UPDATE_COMPLETE
```

Open:

```text
Resources Tab
```

Confirm:

```text
AppServer
AWS::EC2::Instance
```

has been deployed.

Optional:

Navigate to the EC2 Console and verify an instance named:

```text
App Server
```

exists.

---

# Task 4: Delete the Stack

## Objective

Remove all infrastructure created by CloudFormation.

---

## Delete Stack

1. Open CloudFormation Console
2. Select:

```text
Lab
```

3. Click:

```text
Delete
```

4. Confirm:

```text
Delete Stack
```

---

## Verification

Status transitions:

```text
DELETE_IN_PROGRESS
```

to

```text
DELETE_COMPLETE
```

The stack then disappears from the console.

CloudFormation automatically deletes:

- EC2 Instance
- S3 Bucket
- VPC
- Security Groups
- Subnets
- Other stack-managed resources

---

# Key CloudFormation Concepts Learned

## Parameters

Collect user inputs.

Example:

```yaml
Parameters:
```

Used to increase template flexibility.

---

## Resources

Define AWS infrastructure.

Examples:

```yaml
AWS::EC2::VPC
AWS::EC2::Instance
AWS::S3::Bucket
```

---

## Outputs

Display useful values after deployment.

Example:

```yaml
Outputs:
```

---

## References (`!Ref`)

Allow resources to reference each other.

Example:

```yaml
VpcId: !Ref VPC
```

---

## Stack Updates

CloudFormation compares:

```text
Current State
vs
Updated Template
```

and deploys only the required changes.

Benefits:

- Faster deployments
- Lower risk
- Consistent infrastructure

---

## Infrastructure as Code (IaC)

Benefits of CloudFormation:

- Repeatable deployments
- Version-controlled infrastructure
- Automated provisioning
- Easy rollback and recovery
- Consistent environments

---

# CloudFormation Snippets Used

## S3 Bucket

```yaml
S3Bucket:
  Type: AWS::S3::Bucket
```

---

## Dynamic Amazon Linux 2 AMI

```yaml
AmazonLinuxAMIID:
  Type: AWS::SSM::Parameter::Value<AWS::EC2::Image::Id>
  Default: /aws/service/ami-amazon-linux-latest/amzn2-ami-hvm-x86_64-gp2
```

---

## EC2 Instance

```yaml
AppServer:
  Type: AWS::EC2::Instance
  Properties:
    ImageId: !Ref AmazonLinuxAMIID
    InstanceType: t3.micro
    SecurityGroupIds:
      - !Ref AppSecurityGroup
    SubnetId: !Ref PublicSubnet
    Tags:
      - Key: Name
        Value: App Server
```

---

# Summary

In this lab, you:

✅ Created a CloudFormation stack

✅ Deployed a VPC and Security Group

✅ Added an Amazon S3 Bucket

✅ Added an Amazon EC2 Instance

✅ Used Systems Manager Parameter Store to retrieve an AMI

✅ Used `!Ref` to link resources together

✅ Updated an existing stack without redeploying existing infrastructure

✅ Deleted the stack and all associated resources

This lab demonstrates the core workflow of Infrastructure as Code using AWS CloudFormation: **Create → Update → Manage → Delete** infrastructure entirely from templates.