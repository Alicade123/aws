# AWS Lambda Challenge Lab — Word Count with S3, Lambda, SNS, and Email

## Overview

This lab builds an event-driven serverless application that counts the number of words in a text file uploaded to Amazon S3 and reports the result through Amazon SNS email notification.

### Final architecture

```text
                         Upload .txt file
                                │
                                ▼
                       ┌─────────────────┐
                       │       S3        │
                       │     Bucket      │
                       └────────┬────────┘
                                │
                         ObjectCreated
                                │
                                ▼
                    ┌────────────────────────┐
                    │        Lambda          │
                    │                        │
                    │  1. Get filename       │
                    │  2. Get S3 object      │
                    │  3. Read text          │
                    │  4. Count words        │
                    │  5. Publish to SNS     │
                    └───────────┬────────────┘
                                │
                             Publish
                                │
                                ▼
                       ┌─────────────────┐
                       │       SNS       │
                       │      Topic      │
                       └────────┬────────┘
                                │
                              Email
                                │
                                ▼
                         ┌───────────────┐
                         │     Inbox     │
                         └───────────────┘
```

The complete flow is:

**Text file → S3 → Lambda → SNS → Email**

---

# 1. Lab Objectives

By completing this lab, you should be able to:

- Create an AWS Lambda function using Python.
- Read a text file from Amazon S3.
- Count words in the uploaded file.
- Configure S3 to invoke Lambda automatically.
- Create an Amazon SNS topic.
- Subscribe an email endpoint to SNS.
- Publish a message from Lambda to SNS.
- Monitor Lambda executions using CloudWatch.
- Troubleshoot an event-driven AWS architecture.
- Understand IAM execution roles and AWS service permissions.

---

# 2. AWS Resources

The final solution should contain:

| Resource | Purpose |
|---|---|
| S3 bucket | Stores `.txt` files |
| Lambda function | Reads and counts words |
| `LambdaAccessRole` | Gives Lambda required AWS permissions |
| SNS topic | Receives the word-count result |
| SNS email subscription | Sends the result to email |
| S3 → Lambda trigger | Automatically invokes Lambda after upload |
| CloudWatch Logs | Stores Lambda execution logs |

> **Important:** The lab explicitly says to use the existing `LambdaAccessRole`. Do not create a new IAM role unless the lab instructions change.

---

# 3. Phase 1 — Start and Understand the Lab Environment

## Objective

Prepare the AWS environment and identify the Region and IAM role.

## Step 1 — Start the Lab

On the AWS training platform:

1. Click **Start Lab**.
2. Wait until:
   ```text
   Lab status: ready
   ```
3. Close the Start Lab panel.
4. Open the AWS Management Console.

## Step 2 — Identify the AWS Region

Look at the AWS Console top-right and record the Region.

Example:

```text
Region: Europe (Ireland)
Region code: eu-west-1
```

All resources should be created in the **same Region**.

## Step 3 — Understand the IAM Role

The lab provides:

```text
LambdaAccessRole
```

The role has permissions for:

```text
AWSLambdaBasicExecutionRole
AmazonSNSFullAccess
AmazonS3FullAccess
CloudWatchFullAccess
```

Conceptually:

```text
Lambda
   │
   │ assumes
   ▼
LambdaAccessRole
   │
   ├── S3
   ├── SNS
   └── CloudWatch
```

### Why IAM matters

Lambda needs permission to call AWS services.

Instead of putting AWS credentials into the code:

```python
AWS_ACCESS_KEY = "..."
AWS_SECRET_KEY = "..."
```

Lambda uses its execution role.

### Phase 1 expected result

Record:

```text
AWS Region: ______________________________

Region Code: _____________________________

Lambda Execution Role: LambdaAccessRole
```

You should understand:

> Lambda will use `LambdaAccessRole` to access S3, SNS, and CloudWatch.

---

# 4. Phase 2 — Create the SNS Notification System

## Objective

Create an SNS topic that will eventually receive the word-count message from Lambda.

Architecture at this stage:

```text
Lambda
   │
   │ publish
   ▼
SNS Topic
```

---

## Step 1 — Open SNS

In AWS Console:

1. Search for **SNS**.
2. Open **Amazon Simple Notification Service**.
3. Select **Topics**.

## Step 2 — Create the Topic

Choose:

**Create topic**

Use:

```text
Type: Standard
Name: WordCountTopic
```

Click **Create topic**.

## Step 3 — Record the Topic ARN

SNS will show an ARN similar to:

```text
arn:aws:sns:eu-west-1:123456789012:WordCountTopic
```

Record your actual ARN:

```text
SNS Topic Name: WordCountTopic

SNS Topic ARN:
________________________________________________________
```

### What is an ARN?

ARN means **Amazon Resource Name**.

It uniquely identifies an AWS resource.

---

# 5. Phase 3 — Create the SNS Email Subscription

## Objective

Tell SNS where to send notifications.

Architecture:

```text
SNS Topic
   │
   ▼
Email Subscription
   │
   ▼
Your Inbox
```

## Step 1 — Open the Topic

Inside:

**SNS → Topics → WordCountTopic**

Find **Subscriptions**.

Choose:

**Create subscription**

## Step 2 — Configure the Subscription

Use:

```text
Protocol: Email
Endpoint: YOUR_EMAIL_ADDRESS
```

Click:

**Create subscription**

## Step 3 — Confirm the Email

Check your inbox.

Find the SNS subscription confirmation email.

Click:

**Confirm subscription**

The subscription should eventually show:

```text
Status: Confirmed
```

### Why confirmation matters

An email subscription cannot receive SNS messages until it has been confirmed.

### Phase 3 expected result

You should have:

```text
Topic:
WordCountTopic

Subscription:
Protocol: Email
Status: Confirmed
Endpoint: Your email
```

### Concept learned

An SNS topic does not represent one specific email address.

Instead:

```text
SNS Topic
   │
   ├── Email subscription
   ├── SMS subscription
   └── Other subscriptions
```

A publisher sends one message to the topic, and SNS delivers it to the configured subscribers.

---

# 6. Phase 4 — Create the S3 Bucket

## Objective

Create the storage location for the text files.

## Step 1 — Open S3

Search AWS Console for:

```text
S3
```

Open **Amazon S3**.

Select:

**Buckets → Create bucket**

## Step 2 — Bucket Name

S3 bucket names must be globally unique.

Example:

```text
word-count-alicade-2026-8274
```

If unavailable, choose another unique name.

Record it:

```text
S3 Bucket Name:
________________________________________________________
```

## Step 3 — Region

Use the same Region identified in Phase 1.

## Step 4 — Public Access

Keep:

```text
Block all public access
```

enabled.

The application does not require public access.

## Step 5 — Other Settings

For this lab, use the default settings unless the lab environment requires something different.

Versioning is not required for this exercise.

## Step 6 — Create the Bucket

Click:

**Create bucket**

### Phase 4 expected result

You should have:

```text
S3
└── word-count-alicade-2026-xxxx
```

The bucket should be in the same Region as the other resources.

### Concept learned

S3 is an **object storage service**.

A bucket is the container and the uploaded file is an object.

Example:

```text
Bucket
│
├── hello.txt
├── aws.txt
└── lambda.txt
```

---

# 7. Phase 5 — Create the Lambda Function

## Objective

Create the application logic.

Lambda will:

```text
Receive S3 event
      ↓
Identify bucket
      ↓
Identify filename
      ↓
Read file
      ↓
Count words
      ↓
Publish result to SNS
```

---

## Step 1 — Open Lambda

Search:

```text
Lambda
```

Open **AWS Lambda**.

Select:

**Functions → Create function**

## Step 2 — Choose Author from Scratch

Select:

```text
Author from scratch
```

## Step 3 — Basic Configuration

Example:

```text
Function name:
WordCountFunction
```

### Runtime

Choose an available Python runtime supported by your lab.

Use the runtime recommended by the lab if it specifies one.

If no specific version is required, use a currently supported Python runtime offered in the console.

### Architecture

Use:

```text
x86_64
```

unless the lab specifies otherwise.

## Step 4 — Execution Role

Choose:

```text
Use an existing role
```

Select:

```text
LambdaAccessRole
```

Do **not** create another role for this challenge.

## Step 5 — Create Function

Click:

**Create function**

### Phase 5 expected result

You should have:

```text
Lambda
└── WordCountFunction
       │
       └── Execution role:
           LambdaAccessRole
```

---

# 8. Phase 6 — Understand the Lambda Handler

Before writing code, understand the Lambda execution model.

Python Lambda functions commonly use:

```python
def lambda_handler(event, context):
```

Lambda passes:

```text
event
context
```

### `event`

Contains information about what caused the Lambda invocation.

For an S3 event, it contains information about:

- Bucket
- Object key
- Event type
- Object metadata

Conceptually:

```json
{
  "Records": [
    {
      "s3": {
        "bucket": {
          "name": "word-count-bucket"
        },
        "object": {
          "key": "hello.txt"
        }
      }
    }
  ]
}
```

The actual S3 event contains more information.

### `context`

Provides information about the Lambda execution environment, such as:

- Function name
- Request ID
- Memory limit
- Remaining execution time

For this lab, we don't need to use it directly.

---

# 9. Phase 7 — Write the Lambda Code

Replace the default Lambda code with:

```python
import boto3
import urllib.parse

s3 = boto3.client("s3")
sns = boto3.client("sns")

SNS_TOPIC_ARN = "YOUR_SNS_TOPIC_ARN"


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
```

---

# 10. Configure the SNS Topic ARN

Find:

```python
SNS_TOPIC_ARN = "YOUR_SNS_TOPIC_ARN"
```

Replace it with the ARN from Phase 2.

Example:

```python
SNS_TOPIC_ARN = "arn:aws:sns:eu-west-1:123456789012:WordCountTopic"
```

Use **your own** ARN.

Do not copy the example ARN.

---

# 11. Understand the Lambda Code

## 11.1 Import Boto3

```python
import boto3
```

Boto3 is the AWS SDK for Python.

It allows Python code to communicate with AWS services.

---

## 11.2 Create the S3 client

```python
s3 = boto3.client("s3")
```

This creates a client for calling S3 APIs.

---

## 11.3 Create the SNS client

```python
sns = boto3.client("sns")
```

Now the Lambda function can communicate with SNS.

Conceptually:

```text
Python Lambda
   │
   ├── S3 client → S3
   │
   └── SNS client → SNS
```

---

## 11.4 Read the bucket name

```python
bucket = event["Records"][0]["s3"]["bucket"]["name"]
```

The S3 event tells Lambda which bucket caused the event.

---

## 11.5 Read the object key

```python
key = urllib.parse.unquote_plus(
    event["Records"][0]["s3"]["object"]["key"],
    encoding="utf-8"
)
```

The key represents the object name/path.

Examples:

```text
hello.txt
```

or:

```text
input/hello.txt
```

---

## 11.6 Get the S3 object

```python
response = s3.get_object(
    Bucket=bucket,
    Key=key
)
```

This asks S3 for the uploaded object.

---

## 11.7 Read the file

```python
content = response["Body"].read().decode("utf-8")
```

This:

1. Reads the object body.
2. Converts bytes to text using UTF-8.

Example:

```text
Hello AWS Lambda
```

---

## 11.8 Count the words

```python
words = content.split()
word_count = len(words)
```

For:

```text
Hello AWS Lambda
```

Python produces approximately:

```python
["Hello", "AWS", "Lambda"]
```

Then:

```text
len(words) = 3
```

---

## 11.9 Build the required message

```python
message = (
    f"The word count in the {key} file is {word_count}."
)
```

Example:

```text
The word count in the hello.txt file is 3.
```

---

## 11.10 Publish to SNS

```python
sns.publish(
    TopicArn=SNS_TOPIC_ARN,
    Subject="Word Count Result",
    Message=message
)
```

Flow:

```text
Lambda
   │
   │ publish()
   ▼
SNS Topic
   │
   ▼
Confirmed Email Subscription
   │
   ▼
Inbox
```

---

# 12. Phase 8 — Deploy Lambda

After entering the code:

1. Click **Deploy**.
2. Wait for deployment to complete.

### Expected result

The function code is deployed.

However, the automatic S3 trigger has not yet been configured.

---

# 13. Phase 9 — Prepare a Manual Lambda Test

Before connecting S3 automatically, test the Lambda logic manually.

This is an important engineering/QA approach:

```text
Test Lambda
     ↓
Test S3 trigger
     ↓
Test SNS
     ↓
Test complete architecture
```

Testing components progressively makes troubleshooting easier.

---

# 14. Create a Test File

On your computer create:

```text
hello.txt
```

Put this content inside:

```text
Hello AWS Lambda
```

Expected word count:

```text
3
```

Upload `hello.txt` to your S3 bucket.

---

# 15. Create an S3 Test Event

In Lambda:

**Test → Create new event**

Use an S3-style event:

```json
{
  "Records": [
    {
      "s3": {
        "bucket": {
          "name": "YOUR_BUCKET_NAME"
        },
        "object": {
          "key": "hello.txt"
        }
      }
    }
  ]
}
```

Replace:

```text
YOUR_BUCKET_NAME
```

with your actual bucket name.

Save the test event.

---

# 16. Run the Manual Lambda Test

Click:

**Test**

Expected Lambda response:

```json
{
  "statusCode": 200,
  "file": "hello.txt",
  "wordCount": 3,
  "message": "The word count in the hello.txt file is 3."
}
```

You should also receive an email.

Email subject:

```text
Word Count Result
```

Email body:

```text
The word count in the hello.txt file is 3.
```

### Phase 9 expected result

At this point:

```text
S3
 │
 │ manually represented by test event
 ▼
Lambda
 │
 ▼
SNS
 │
 ▼
Email
```

works.

The S3 bucket still does not automatically invoke Lambda.

---

# 17. Phase 10 — Configure S3 → Lambda Trigger

## Objective

Automate the invocation.

Before:

```text
Upload file
     ↓
S3

Lambda
     ↑
manual test
```

After:

```text
Upload file
     ↓
S3
     │
     │ ObjectCreated
     ▼
Lambda
```

---

## Step 1 — Open Lambda

Go to:

**Lambda → Functions → WordCountFunction**

Find:

**Function overview**

Choose:

**Add trigger**

---

## Step 2 — Select S3

Choose:

```text
Source:
S3
```

Select your S3 bucket.

---

## Step 3 — Configure Event Type

Choose the available object-created event option, such as:

```text
All object create events
```

or:

```text
Object Created
```

The exact console wording can vary.

---

## Step 4 — Restrict the Trigger to `.txt`

Configure the suffix:

```text
.txt
```

This means:

```text
hello.txt       → Lambda executes
test.txt        → Lambda executes
document.pdf    → Lambda does not execute
image.jpg       → Lambda does not execute
```

This prevents unnecessary Lambda executions.

---

## Step 5 — Recursive Invocation Warning

AWS may show a warning about recursive invocation.

A recursive loop could happen if:

```text
S3
 ↓
Lambda
 ↓
writes a file to same S3 bucket
 ↓
S3
 ↓
Lambda
 ↓
...
```

Our Lambda only **reads** from S3 and publishes to SNS, so it does not create this loop.

---

## Step 6 — Add the Trigger

Click:

**Add**

### Phase 10 expected result

Lambda should show the S3 trigger.

Conceptually:

```text
S3 Bucket
     │
     │ ObjectCreated
     ▼
WordCountFunction
```

---

# 18. Phase 11 — End-to-End Test

Now test the complete architecture.

Create:

```text
test1.txt
```

Content:

```text
AWS Lambda makes serverless computing simple.
```

Expected word count:

```text
AWS          1
Lambda       2
makes        3
serverless   4
computing    5
simple       6
```

Expected:

```text
6
```

Upload `test1.txt` to S3.

Do **not** manually invoke Lambda.

---

# 19. Expected End-to-End Flow

Within a short time:

```text
test1.txt uploaded
       ↓
S3 detects ObjectCreated
       ↓
S3 invokes Lambda
       ↓
Lambda receives event
       ↓
Lambda identifies test1.txt
       ↓
Lambda gets object from S3
       ↓
Lambda reads content
       ↓
Lambda counts 6 words
       ↓
Lambda publishes to SNS
       ↓
SNS sends email
```

Expected email:

```text
Subject:
Word Count Result

Body:
The word count in the test1.txt file is 6.
```

### Phase 11 expected result

The complete system works automatically:

```text
S3 → Lambda → SNS → Email
```

This is the main success criterion of the lab.

---

# 20. Phase 12 — Test Multiple Files

Do not stop after one successful test.

Test several files with known expected results.

| Test | File | Content | Expected |
|---|---|---|---:|
| TC01 | `hello.txt` | `Hello AWS Lambda` | 3 |
| TC02 | `aws.txt` | `Amazon Web Services provides cloud computing services.` | 7 |
| TC03 | `lambda.txt` | `Lambda runs code without managing servers.` | 6 |
| TC04 | `empty.txt` | Empty file | 0 |
| TC05 | `spaces.txt` | Multiple spaces/newlines | Verify |
| TC06 | `document.pdf` | PDF | Lambda should not trigger |
| TC07 | `HELLO.TXT` | Text file | Verify suffix-filter behavior |

## Important testing principle

Calculate the expected result **before** uploading the file.

Then compare:

```text
Expected result
       vs
Actual email result
```

This is especially useful from a QA perspective.

---

# 21. Phase 13 — CloudWatch Logs

Lambda execution logs are available through Amazon CloudWatch Logs.

Go to:

**Lambda → WordCountFunction → Monitor**

Then:

**View CloudWatch logs**

You should find a log group similar to:

```text
/aws/lambda/WordCountFunction
```

Your code contains:

```python
print(f"Processing file: {key}")
print(f"Bucket: {bucket}")
print(message)
```

So logs should contain information similar to:

```text
Processing file: hello.txt
Bucket: word-count-alicade-2026-xxxx

The word count in the hello.txt file is 3.
```

---

# 22. Why CloudWatch Is Important

CloudWatch helps determine where an event-driven application failed.

For example:

```text
S3 upload
    ↓
S3 trigger
    ↓
Lambda
    ↓
❌ AccessDenied
```

CloudWatch can reveal the Lambda-side failure.

For troubleshooting, think:

```text
S3
 │
 ├── Did the object upload?
 │
 ▼
S3 Trigger
 │
 ├── Did Lambda invoke?
 │
 ▼
Lambda
 │
 ├── Did it read S3?
 │
 ├── Did it count correctly?
 │
 └── Did it publish to SNS?
 │
 ▼
SNS
 │
 ├── Is subscription confirmed?
 │
 ▼
Email
```

---

# 23. Troubleshooting Guide

## Problem 1 — No email

Check:

```text
SNS
→ Topics
→ WordCountTopic
→ Subscriptions
```

Make sure:

```text
Status = Confirmed
```

If it is:

```text
Pending confirmation
```

confirm the subscription from the email.

---

## Problem 2 — Lambda is not invoked

Check:

```text
Lambda
→ WordCountFunction
→ Configuration
→ Triggers
```

Verify:

```text
Bucket = correct bucket
Event = ObjectCreated
Suffix = .txt
```

Also confirm that you uploaded a new object after configuring the trigger.

---

## Problem 3 — AccessDenied

Check:

```text
Lambda
→ Configuration
→ Permissions
→ Execution role
```

It should use:

```text
LambdaAccessRole
```

The role must have the required S3, SNS, and CloudWatch permissions.

---

## Problem 4 — NoSuchKey

Lambda attempted to retrieve an S3 object that doesn't exist under the specified key.

Verify:

```text
Bucket:
correct bucket

Key:
correct filename/path
```

For example:

```text
Bucket:
word-count-bucket

Key:
hello.txt
```

Make sure the object exists exactly at that location.

---

## Problem 5 — Word count is incorrect

The implementation uses:

```python
content.split()
```

This splits on whitespace.

For:

```text
Hello   AWS
Lambda
```

Python produces approximately:

```python
["Hello", "AWS", "Lambda"]
```

Result:

```text
3
```

Punctuation remains attached to words:

```text
Hello, world!
```

becomes approximately:

```python
["Hello,", "world!"]
```

Result:

```text
2
```

For this lab, whitespace-based counting is sufficient unless the instructor specifies another definition of "word."

---

# 24. Understand IAM

One of the most important concepts in this lab is the Lambda execution role.

Without appropriate permissions:

```text
Lambda
   │
   ├── ❌ S3
   └── ❌ SNS
```

With `LambdaAccessRole`:

```text
Lambda
   │
   ▼
LambdaAccessRole
   │
   ├── ✅ S3
   ├── ✅ SNS
   └── ✅ CloudWatch
```

The execution role determines what AWS resources/services the Lambda function is authorized to access.

---

# 25. Authentication vs Authorization

A useful distinction:

### Authentication

```text
Who are you?
```

### Authorization

```text
What are you allowed to do?
```

IAM is primarily involved in identity and authorization.

Lambda should not contain hard-coded AWS credentials.

Avoid:

```python
AWS_ACCESS_KEY = "..."
AWS_SECRET_KEY = "..."
```

Instead:

```text
Lambda
   ↓
Execution Role
   ↓
AWS-provided credentials
   ↓
Boto3
   ↓
S3 / SNS / CloudWatch
```

---

# 26. Understand Each AWS Service

## Amazon S3

**Purpose:** Object storage.

In this lab:

```text
Stores .txt files
```

---

## AWS Lambda

**Purpose:** Run code without managing servers.

In this lab:

```text
Reads file
Counts words
Publishes result
```

---

## Amazon SNS

**Purpose:** Publish/subscribe messaging.

In this lab:

```text
Receives word-count message
        ↓
Delivers email
```

---

## IAM

**Purpose:** Identity and access control.

In this lab:

```text
LambdaAccessRole
```

provides Lambda with permissions.

---

## CloudWatch

**Purpose:** Monitoring and logging.

In this lab:

```text
Lambda execution logs
```

help troubleshoot failures.

---

# 27. Understand the Complete Event Flow

Suppose:

```text
hello.txt
```

contains:

```text
Hello AWS Lambda
```

## Step 1 — S3

S3 stores:

```text
hello.txt
```

## Step 2 — S3 generates event

An object-created event is generated.

## Step 3 — Lambda receives event

Lambda receives information including:

```text
bucket = word-count-alicade-2026-xxxx
key = hello.txt
```

## Step 4 — Lambda gets object

Lambda calls:

```python
s3.get_object(...)
```

## Step 5 — Lambda reads content

```text
Hello AWS Lambda
```

## Step 6 — Lambda counts

```text
3
```

## Step 7 — Lambda creates message

```text
The word count in the hello.txt file is 3.
```

## Step 8 — Lambda publishes

```python
sns.publish(...)
```

## Step 9 — SNS

SNS finds the confirmed email subscription.

## Step 10 — Email

You receive:

```text
Subject:
Word Count Result

The word count in the hello.txt file is 3.
```

---

# 28. Final Verification Checklist

Before submitting the lab:

- [ ] Lab started successfully.
- [ ] AWS Region identified.
- [ ] All resources created in the same Region.
- [ ] Existing `LambdaAccessRole` used.
- [ ] SNS topic created.
- [ ] SNS email subscription created.
- [ ] SNS email subscription confirmed.
- [ ] S3 bucket created.
- [ ] Lambda function created.
- [ ] Python runtime configured.
- [ ] Lambda code deployed.
- [ ] SNS Topic ARN configured correctly.
- [ ] Lambda can read S3 objects.
- [ ] Lambda can publish to SNS.
- [ ] S3 trigger configured.
- [ ] Trigger uses ObjectCreated.
- [ ] `.txt` suffix configured.
- [ ] Test files uploaded.
- [ ] Lambda automatically invoked.
- [ ] Word counts are correct.
- [ ] Email received.
- [ ] CloudWatch logs verified.
- [ ] Multiple files tested.
- [ ] Lambda screenshot captured.
- [ ] Result email forwarded to instructor.

---

# 29. Recommended Evidence / Screenshots

Capture evidence for the major components.

## Screenshot 1 — Lambda Overview

Show:

```text
WordCountFunction
```

and its S3 trigger.

## Screenshot 2 — Lambda Code

Show the code containing:

```python
s3.get_object(...)
```

and:

```python
sns.publish(...)
```

## Screenshot 3 — S3

Show:

```text
Bucket
└── hello.txt
```

## Screenshot 4 — SNS

Show:

```text
WordCountTopic
└── Confirmed email subscription
```

## Screenshot 5 — Email

Show:

```text
Subject: Word Count Result

The word count in the hello.txt file is 3.
```

These screenshots provide evidence of the complete architecture.

---

# 30. Learning Goals

Do not measure success only by:

> "I completed the AWS lab."

Instead, be able to explain these five concepts without looking at the instructions.

## 1. What is Lambda?

> AWS Lambda is a serverless compute service that executes code in response to events without requiring you to manage the underlying servers.

## 2. What is an S3 event?

> An S3 event is a notification describing something that happened to an object, such as an object being created.

## 3. How does S3 invoke Lambda?

```text
S3 ObjectCreated
       ↓
S3 Event Notification
       ↓
Lambda
```

## 4. How does Lambda access S3 and SNS?

```text
Lambda
   ↓
Execution Role
   ↓
IAM permissions
   ↓
AWS services
```

## 5. How does the email happen?

```text
Lambda
   ↓
sns.publish()
   ↓
SNS Topic
   ↓
Confirmed Email Subscription
   ↓
Email
```

If you can explain these five, you have learned the architecture rather than simply completing the lab.

---

# 31. Recommended Documentation to Study

Study the following AWS documentation alongside the lab.

### Lambda Python Handler

Learn:

- `lambda_handler(event, context)`
- Lambda events
- Boto3
- Lambda execution environment

https://docs.aws.amazon.com/lambda/latest/dg/python-handler.html

### S3 → Lambda

Learn:

- S3 triggers
- S3 event structure
- Lambda integration
- Same-Region requirements

https://docs.aws.amazon.com/lambda/latest/dg/with-s3-example.html

### S3 Event Notifications

Learn:

- Object-created events
- Event notification configuration
- Lambda destinations

https://docs.aws.amazon.com/AmazonS3/latest/userguide/EventNotifications.html

### SNS Subscriptions

Learn:

- SNS topics
- Subscriptions
- Email confirmation
- Publish/subscribe architecture

https://docs.aws.amazon.com/sns/latest/dg/sns-create-subscribe-endpoint-to-topic.html

### Boto3 SNS `publish()`

Learn:

- `sns.publish()`
- Topic ARN
- Message
- Subject

https://docs.aws.amazon.com/boto3/latest/reference/services/sns/client/publish.html

---

# 32. Recommended Practice Extension

After completing the official lab, rebuild the same project as a second exercise.

## Level 1 — Official Lab

```text
S3
 ↓
Lambda
 ↓
SNS
 ↓
Email
```

Use:

```text
LambdaAccessRole
```

as required by the training environment.

---

## Level 2 — Improve the Application

Add:

- Least-privilege IAM permissions.
- SNS Topic ARN as a Lambda environment variable.
- Better error handling.
- Structured CloudWatch logging.
- Validation for `.txt` files.
- Handling of empty files.
- Unit tests for word counting.
- Better handling of malformed S3 events.

Architecture:

```text
S3
 ↓
Lambda
 │
 ├── Validation
 ├── Error handling
 ├── Logging
 └── Word counting
 ↓
SNS
 ↓
Email
```

---

## Level 3 — Infrastructure as Code

Rebuild the application using:

```text
Terraform
```

or:

```text
AWS SAM
```

Then add:

```text
GitHub
   ↓
CI/CD
   ↓
Automated tests
   ↓
AWS deployment
```

This turns the training exercise into a practical serverless engineering project.

---

# 33. Final Architecture Summary

The complete application is:

```text
                         USER
                           │
                           │ Upload hello.txt
                           ▼
                    ┌──────────────┐
                    │      S3      │
                    │    Bucket    │
                    └──────┬───────┘
                           │
                           │ ObjectCreated
                           ▼
                    ┌──────────────┐
                    │    Lambda    │
                    │              │
                    │ Python       │
                    │              │
                    │ Get S3 file  │
                    │ Read content  │
                    │ Count words  │
                    │ Publish SNS  │
                    └──────┬───────┘
                           │
                           │ sns.publish()
                           ▼
                    ┌──────────────┐
                    │     SNS      │
                    │ WordCountTopic│
                    └──────┬───────┘
                           │
                           │ Subscription
                           ▼
                    ┌──────────────┐
                    │    Email     │
                    └──────────────┘

                    ┌──────────────┐
                    │ CloudWatch   │
                    │     Logs     │
                    └──────▲───────┘
                           │
                           │ Lambda logs
                           │
                       Lambda

                    ┌──────────────┐
                    │     IAM      │
                    │LambdaAccessRole│
                    └──────▲───────┘
                           │
                           │ permissions
                           │
                       Lambda
```

---

# 34. Lab Completion Criteria

The lab is complete when:

```text
1. You upload a .txt file to S3
                ↓
2. S3 automatically invokes Lambda
                ↓
3. Lambda reads the uploaded file
                ↓
4. Lambda counts its words
                ↓
5. Lambda publishes the result to SNS
                ↓
6. SNS sends the result to your email
```

The final email must use:

```text
Subject:
Word Count Result
```

and:

```text
The word count in the <textFileName> file is nnn.
```

Example:

```text
The word count in the hello.txt file is 3.
```

---

# 35. Lab Shutdown

When finished:

1. Verify your evidence/screenshots.
2. Forward the required email to your instructor.
3. Provide the Lambda screenshot requested by the lab.
4. At the top of the lab instructions choose:
   **End Lab**
5. Choose:
   **Yes**
6. Wait for the lab termination message.
7. Close the message.

> Do not continue experimenting after ending the lab because the temporary AWS account/resources may be terminated.

---

## Quick Reference

```text
AWS SERVICES
─────────────

S3        → Stores text files
Lambda    → Processes files
SNS       → Sends notifications
IAM       → Controls permissions
CloudWatch→ Logs/monitoring


ARCHITECTURE
────────────

Upload .txt
    ↓
   S3
    ↓
ObjectCreated
    ↓
 Lambda
    ↓
Read S3 object
    ↓
Count words
    ↓
sns.publish()
    ↓
   SNS
    ↓
 Email


KEY RESOURCES
─────────────

Lambda Function:
WordCountFunction

IAM Role:
LambdaAccessRole

SNS Topic:
WordCountTopic

S3 Bucket:
<your-unique-bucket-name>

SNS Topic ARN:
<your-topic-arn>


EXPECTED EMAIL
──────────────

Subject:
Word Count Result

Body:
The word count in the hello.txt file is 3.
```
