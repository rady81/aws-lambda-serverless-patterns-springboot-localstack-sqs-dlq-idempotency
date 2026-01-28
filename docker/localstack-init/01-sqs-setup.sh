#!/usr/bin/env bash
set -euo pipefail

# Purpose:
# Creates SQS main queue + DLQ and attaches a redrive policy to simulate retries and poison message routing.

awslocal sqs create-queue --queue-name student-card-application-dlq

DLQ_URL=$(awslocal sqs get-queue-url --queue-name student-card-application-dlq --query 'QueueUrl' --output text)
DLQ_ARN=$(awslocal sqs get-queue-attributes --queue-url "$DLQ_URL" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)

awslocal sqs create-queue --queue-name student-card-application-queue

MAIN_URL=$(awslocal sqs get-queue-url --queue-name student-card-application-queue --query 'QueueUrl' --output text)

# After 3 receives without successful delete, route to DLQ.
REDRIVE_POLICY="{\"deadLetterTargetArn\":\"$DLQ_ARN\",\"maxReceiveCount\":\"3\"}"

awslocal sqs set-queue-attributes \
  --queue-url "$MAIN_URL" \
  --attributes RedrivePolicy="$REDRIVE_POLICY",VisibilityTimeout="15"

echo "SQS queues created:"
echo "MAIN: $MAIN_URL"
echo "DLQ:  $DLQ_URL"
