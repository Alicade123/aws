import { EC2Client, AuthorizeSecurityGroupIngressCommand, CreateSecurityGroupCommand, RunInstancesCommand } from "@aws-sdk/client-ec2";

const client = new EC2Client({ region: "us-west-2" });

const main = async () => {
  await client.send(new CreateSecurityGroupCommand({
      GroupName: "Bastion security group",
      Description: "Permit SSH connections",
      VpcId: "vpc-0c17018e540ab9a87",
    }));

  await client.send(new AuthorizeSecurityGroupIngressCommand({
      GroupId: "sg-preview-1",
      IpPermissions: [{
        IpProtocol: "tcp",
        FromPort: 22,
        ToPort: 22,
        IpRanges: [{
          CidrIp: "0.0.0.0/0",
        }],
      }],
    }));

  await client.send(new RunInstancesCommand({
      MaxCount: 1,
      MinCount: 1,
      ImageId: "ami-08b7b9fdd7a1edf3d",
      InstanceType: "t3.micro",
      EbsOptimized: true,
      NetworkInterfaces: [{
        SubnetId: "subnet-04ab13936ee959864",
        AssociatePublicIpAddress: true,
        DeviceIndex: 0,
        Groups: ["sg-preview-1"],
      }],
      CreditSpecification: {
        CpuCredits: "unlimited",
      },
      TagSpecifications: [{
        ResourceType: "instance",
        Tags: [{
          Key: "Name",
          Value: "Bastion host",
        }],
      }],
      IamInstanceProfile: {
        Arn: "arn:aws:iam::192897740669:instance-profile/Bastion-Role",
      },
      MetadataOptions: {
        HttpEndpoint: "enabled",
        HttpPutResponseHopLimit: 2,
        HttpTokens: "required",
      },
      PrivateDnsNameOptions: {
        HostnameType: "ip-name",
        EnableResourceNameDnsARecord: false,
        EnableResourceNameDnsAAAARecord: false,
      },
    }));
};

main();
