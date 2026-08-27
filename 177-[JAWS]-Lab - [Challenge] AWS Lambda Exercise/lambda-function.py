import boto3
import urllib.parse

s3 = boto3.client("s3")
sns = boto3.client("sns")

SNS_TOPIC_ARN = "/data.json/ARN-SNS"

def lambda_handler(event, context):

    # Get bucket name
    bucket = event["Records"][0]["s3"]["bucket"]["name"]

    # Get uploaded file name
    key = urllib.parse.unquote_plus(
        event["Records"][0]["s3"]["object"]["key"],
        encoding="utf-8"
    )

    print(f"Processing file: {key}")
    print(f"Bucket: {bucket}")

    # Get the file from S3
    response = s3.get_object(
        Bucket=bucket,
        Key=key
    )

    # Read file content
    content = response["Body"].read().decode("utf-8")

    # Count words
    words = content.split()
    word_count = len(words)

    # Create required message
    message = (
        f"The word count in the {key} file is {word_count}."
    )

    print(message)

    # Send result to SNS
    sns.publish(
        TopicArn=SNS_TOPIC_ARN,
        Subject="Word Count Result",
        Message=message
    )

    return {
        "statusCode": 200,
        "file": key,
        "wordCount": word_count,
        "message": message
    }