import boto3

client = boto3.client('ec2', region_name='us-west-2')

response = client.run_instances(
    MaxCount=1,
    MinCount=1,
    ImageId="ami-08b7b9fdd7a1edf3d",
    InstanceType="t3.micro",
    EbsOptimized=True,
    UserData="IyEvYmluL2Jhc2gKCmRuZiBpbnN0YWxsIC15IGh0dHBkCgpzeXN0ZW1jdGwgZW5hYmxlIGh0dHBkCnN5c3RlbWN0bCBzdGFydCBodHRwZAoKY2htb2QgNzc3IC92YXIvd3d3L2h0bWwKCmVjaG8gImh0dHBkIGluc3RhbGxhdGlvbiBjb21wbGV0ZWQgc3VjY2Vzc2Z1bGx5IiA+IC92YXIvbG9nL2VjMi1jaGFsbGVuZ2UubG9n",
    BlockDeviceMappings=[{"DeviceName": "/dev/xvda", "Ebs": {"Encrypted": False, "DeleteOnTermination": True, "SnapshotId": "snap-0568ee1c2f2e51a99", "VolumeSize": 8, "VolumeType": "gp2"}}],
    NetworkInterfaces=[{"SubnetId": "subnet-0911af01341b68009", "AssociatePublicIpAddress": True, "DeviceIndex": 0, "Groups": ["sg-05d35e0c29e782241"]}],
    CreditSpecification={"CpuCredits": "unlimited"},
    TagSpecifications=[{"ResourceType": "instance", "Tags": [{"Key": "Name", "Value": "EC2-Challenge-WebServer"}]}],
    MetadataOptions={"HttpEndpoint": "enabled", "HttpPutResponseHopLimit": 2, "HttpTokens": "required"},
    PrivateDnsNameOptions={"HostnameType": "ip-name", "EnableResourceNameDnsARecord": False, "EnableResourceNameDnsAAAARecord": False}
)
