import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressResponse;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;

Ec2AsyncClient ec2Client = Ec2AsyncClient.builder()
    .region(Region.of("us-west-2"))
    .build();

CreateSecurityGroupRequest request1 = CreateSecurityGroupRequest.builder()
    .groupName("Bastion security group")
    .description("Permit SSH connections")
    .vpcId("vpc-0c17018e540ab9a87")
    .build();

CompletableFuture<CreateSecurityGroupResponse> response1 = ec2Client.createSecurityGroup(request1);

AuthorizeSecurityGroupIngressRequest request2 = AuthorizeSecurityGroupIngressRequest.builder()
    .groupId("sg-preview-1")
    .ipPermissions(Arrays.asList(IpPermissions.builder()
            .ipProtocol("tcp")
            .fromPort(22)
            .toPort(22)
            .ipRanges(Arrays.asList(IpRanges.builder()
            .cidrIp("0.0.0.0/0")
            .build()))
            .build()))
    .build();

CompletableFuture<AuthorizeSecurityGroupIngressResponse> response2 = response1.thenCompose(r -> ec2Client.authorizeSecurityGroupIngress(request2));

RunInstancesRequest request3 = RunInstancesRequest.builder()
    .maxCount(1)
    .minCount(1)
    .imageId("ami-08b7b9fdd7a1edf3d")
    .instanceType("t3.micro")
    .ebsOptimized(true)
    .networkInterfaces(Arrays.asList(NetworkInterfaces.builder()
            .subnetId("subnet-04ab13936ee959864")
            .associatePublicIpAddress(true)
            .deviceIndex(0)
            .groups(Arrays.asList("sg-preview-1"))
            .build()))
    .creditSpecification(CreditSpecification.builder()
            .cpuCredits("unlimited")
            .build())
    .tagSpecifications(Arrays.asList(TagSpecifications.builder()
            .resourceType("instance")
            .tags(Arrays.asList(Tags.builder()
            .key("Name")
            .value("Bastion host")
            .build()))
            .build()))
    .iamInstanceProfile(IamInstanceProfile.builder()
            .arn("arn:aws:iam::192897740669:instance-profile/Bastion-Role")
            .build())
    .metadataOptions(MetadataOptions.builder()
            .httpEndpoint("enabled")
            .httpPutResponseHopLimit(2)
            .httpTokens("required")
            .build())
    .privateDnsNameOptions(PrivateDnsNameOptions.builder()
            .hostnameType("ip-name")
            .enableResourceNameDnsARecord(false)
            .enableResourceNameDnsAaaaRecord(false)
            .build())
    .build();

CompletableFuture<RunInstancesResponse> response3 = response2.thenCompose(r -> ec2Client.runInstances(request3));
