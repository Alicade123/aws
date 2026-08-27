import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;

Ec2AsyncClient ec2Client = Ec2AsyncClient.builder()
    .region(Region.of("us-west-2"))
    .build();

RunInstancesRequest request = RunInstancesRequest.builder()
    .maxCount(1)
    .minCount(1)
    .imageId("ami-08b7b9fdd7a1edf3d")
    .instanceType("t3.micro")
    .ebsOptimized(true)
    .userData("IyEvYmluL2Jhc2gKCmRuZiBpbnN0YWxsIC15IGh0dHBkCgpzeXN0ZW1jdGwgZW5hYmxlIGh0dHBkCnN5c3RlbWN0bCBzdGFydCBodHRwZAoKY2htb2QgNzc3IC92YXIvd3d3L2h0bWwKCmVjaG8gImh0dHBkIGluc3RhbGxhdGlvbiBjb21wbGV0ZWQgc3VjY2Vzc2Z1bGx5IiA+IC92YXIvbG9nL2VjMi1jaGFsbGVuZ2UubG9n")
    .blockDeviceMappings(Arrays.asList(BlockDeviceMappings.builder()
            .deviceName("/dev/xvda")
            .ebs(Ebs.builder()
            .encrypted(false)
            .deleteOnTermination(true)
            .snapshotId("snap-0568ee1c2f2e51a99")
            .volumeSize(8)
            .volumeType("gp2")
            .build())
            .build()))
    .networkInterfaces(Arrays.asList(NetworkInterfaces.builder()
            .subnetId("subnet-0911af01341b68009")
            .associatePublicIpAddress(true)
            .deviceIndex(0)
            .groups(Arrays.asList("sg-05d35e0c29e782241"))
            .build()))
    .creditSpecification(CreditSpecification.builder()
            .cpuCredits("unlimited")
            .build())
    .tagSpecifications(Arrays.asList(TagSpecifications.builder()
            .resourceType("instance")
            .tags(Arrays.asList(Tags.builder()
            .key("Name")
            .value("EC2-Challenge-WebServer")
            .build()))
            .build()))
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

CompletableFuture<RunInstancesResponse> response = ec2Client.runInstances(request);
