import boto3

client = boto3.client('ec2', region_name='us-west-2')

response1 = client.create_security_group(
    GroupName="Bastion security group",
    Description="Permit SSH connections",
    VpcId="vpc-0c17018e540ab9a87"
)

response2 = client.authorize_security_group_ingress(
    GroupId="sg-preview-1",
    IpPermissions=[{"IpProtocol": "tcp", "FromPort": 22, "ToPort": 22, "IpRanges": [{"CidrIp": "0.0.0.0/0"}]}]
)

response3 = client.run_instances(
    MaxCount=1,
    MinCount=1,
    ImageId="ami-08b7b9fdd7a1edf3d",
    InstanceType="t3.micro",
    EbsOptimized=True,
    NetworkInterfaces=[{"SubnetId": "subnet-04ab13936ee959864", "AssociatePublicIpAddress": True, "DeviceIndex": 0, "Groups": ["sg-preview-1"]}],
    CreditSpecification={"CpuCredits": "unlimited"},
    TagSpecifications=[{"ResourceType": "instance", "Tags": [{"Key": "Name", "Value": "Bastion host"}]}],
    IamInstanceProfile={"Arn": "arn:aws:iam::192897740669:instance-profile/Bastion-Role"},
    MetadataOptions={"HttpEndpoint": "enabled", "HttpPutResponseHopLimit": 2, "HttpTokens": "required"},
    PrivateDnsNameOptions={"HostnameType": "ip-name", "EnableResourceNameDnsARecord": False, "EnableResourceNameDnsAAAARecord": False}
)
