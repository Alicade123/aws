# Task A: Launching an EC2 instance using the AWS CLI

## Script explanation

### Step 1: Retrieve the AMI to use

```bash
#Set the Region
AZ=`curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone`
export AWS_DEFAULT_REGION=${AZ::-1}
#Retrieve latest Linux AMI
AMI=$(aws ssm get-parameters --names /aws/service/ami-amazon-linux-latest/amzn2-ami-hvm-x86_64-gp2 --query 'Parameters[0].[Value]' --output text)
echo $AMI
```

- The script retrieves the Availability Zone for the running instance using instance metadata.

- The script retrieves the Region from the Availability Zone and exports it into the environment for subsequent use.

- The script calls the AWS Systems Manager (indicated with the ssm command) and uses the get-parameters command to retrieve the AMI ID from Parameter Store.

- The AMI requested was for Amazon Linux 2 (which is the same as the one used for bastion host).

- The AMI ID has been stored in an environment variable called AMI.

> [!CAUTION] If your EC2 Instance Connect session disconnects, it will lose the information stored in the environment variables. Refresh your browser to reconnect. If you do so, you will need to re-run all of the steps in this task, starting with the commands in this step, to obtain the AMI ID.

### Step 2: Retrieve the subnet to use

You launch the new instance in the public subnet. When launching an instance, you can specify the subnet ID.

To retrieve the subnet ID for the public subnet, run the following command:

```bash
SUBNET=$(aws ec2 describe-subnets --filters 'Name=tag:Name,Values=Public Subnet' --query Subnets[].SubnetId --output text)
echo $SUBNET
```

- This script runs the aws ec2 command with the describe-subnets subcommand to retrieve the subnet ID of the subnet named Public Subnet.

### Step 3: Retrieve the security group to use

This lab includes a web security group, which allows inbound HTTP requests.

Run the following command:

```bash
SG=$(aws ec2 describe-security-groups --filters Name=group-name,Values=WebSecurityGroup --query SecurityGroups[].GroupId --output text)
echo $SG
```

- The script runs the aws ec2 command with the describe-security-groups subcommand to retrieve the security group ID of the web security group.

### Step 4: Download a user data script

In this step, you launch an instance that acts as a web server. To install and configure the web server, you provide a user data script that automatically runs when the instance launches.

- To download the user data script, run the following command:

```bash
wget https://aws-tc-largeobjects.s3.us-west-2.amazonaws.com/CUR-TF-100-RSJAWS-1-23732/171-lab-JAWS-create-ec2/s3/UserData.txt
```

To view the contents of the script, run the following command:

```bash
cat UserData.txt
```

The script does the following:

- Installs a web server

- Downloads a .zip file containing the web application

- Installs the web application

### Step 5: Launch the instance

You now have all the necessary information required to launch the web server instance.

Run the following command:

```bash
INSTANCE=$(\
aws ec2 run-instances \
--image-id $AMI \
--subnet-id $SUBNET \
--security-group-ids $SG \
--user-data file:///home/ec2-user/UserData.txt \
--instance-type t3.micro \
--tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=Web Server}]' \
--query 'Instances[*].InstanceId' \
--output text \
)
echo $INSTANCE
```

The run-instances command launches a new instance using these parameters:

- [ ] **Image** : Uses the AMI value obtained earlier from Parameter Store

- [ ] **Subnet** : Specifies the public subnet retrieved earlier and, by association, the VPC in which to launch the instance

- [ ] **Security group** : Uses the web security group retrieved earlier, which permits HTTP access

- [ ] **User data** : References the user data script that you downloaded, which installs the web application

- [ ] **Instance type** : Specifies the type of instance to launch

- [ ] **Tags** : Assigns a name tag with the value of Web Server

The query parameter specifies that the command should return the instance ID once the instance is launched.

The output parameter specifies that the output of the command should be in text. Other output options are json and table.

Note: The ID of the new instance has been stored in the INSTANCE environment variable.

### Step 6: Wait for the instance to be ready

You can monitor the status of the instance by using the AWS Management Console or by querying the status by using the AWS CLI.

Run the following command:

```bash
aws ec2 describe-instances --instance-ids $INSTANCE
```

All information related to the instance is displayed in JSON format. This information includes the instance status.
You can retrieve specific information by using the query parameter.

Run the following command:

```bash
aws ec2 describe-instances --instance-ids $INSTANCE --query 'Reservations[].Instances[].State.Name' --output text
```

This command is the same as the command in the previous step, but rather than displaying all information about the instance, this command displays only the name of the instance state.

This command displays a status of pending or running.

Run this command again until it returns a status of running.

### Step 7: Test the web server

You can now test that the web server is working. You can retrieve a URL to the instance through the AWS CLI.

Run the following command:

```bash
aws ec2 describe-instances --instance-ids $INSTANCE --query Reservations[].Instances[].PublicDnsName --output text
```

This command returns the public IPv4 Domain Name System (DNS) name of the instance.

Copy the DNS name that is displayed. It should be similar to the following: 

```bash
ec2-35-11-22-33.us-west-2.compute.amazonaws.com
```

Paste the DNS name into a new web browser tab, and then press Enter.

A web page should be displayed, which demonstrates that the web server was successfully launched and configured.
You can also see the instance on the Amazon EC2 management console.

Return to the web browser tab containing the Amazon EC2 management console. In the left navigation pane, choose Instances, and choose  refresh.

The list should now include the Web Server instance that you launched by using the CLI command.

As you see in this task, the AWS CLI makes it possible to programmatically access and control AWS services. You can place these commands in a script and run them as a standard process to deploy consistent, reliable infrastructure with minimal scope for human error.

#### Which method should you use?

1. Launch from the management console when you quickly need to launch a one-off or temporary instance.

1. Launch by using a script when you need to automate the creation of an instance in a repeatable, reliable manner.

1. Launch by using CloudFormation when you want to launch related resources together.

```bash
SUBNET=$(aws ec2 describe-subnets --filters 'Name=tag:Name,Values=Public Subnet' --query Subnets[].SubnetId --output text)
echo $SUBNET
```

- This script runs the aws ec2 command with the describe-subnets subcommand to retrieve the subnet ID of the subnet named Public Subnet.
