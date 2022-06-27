import json
import boto3
from opentelemetry import trace



#BotocoreInstrumentor().instrument()

def lambda_handler(event, context):
   
   tracer = trace.get_tracer(__name__)

   notification = "Here is the SNS notification for Lambda function tutorial."
   with tracer.start_as_current_span("Staring Function") as startingSpan:
     print(startingSpan)
   with tracer.start_as_current_span("Sending Message to SNS") as span:
      span.set_attribute("messageTxt", notification)
      client = boto3.client('sns')
      messagingTraceId = trace.span.format_trace_id(span.get_span_context().trace_id)
      messagingSpanId = trace.span.format_span_id(span.get_span_context().span_id)
      #messagingTraceId = str(span.get_span_context().trace_id)
      #messagingSpanId = str(span.get_span_context().span_id)
      print(messagingTraceId + " - " + messagingSpanId)
      response = client.publish (
         TargetArn = "arn:aws:sns:us-east-1:537309256512:LabItau_SNS",
         Message = json.dumps({'default': notification}),
         MessageAttributes={
              'trace_id': {
                  'DataType': 'String',
                  'StringValue': messagingTraceId # sending traceId as message attribute
              },
              'span_id': {
                  'DataType': 'String',
                  'StringValue': messagingSpanId # sending spanId as message attribute
              }
          },
         MessageStructure = 'json'
      )
      span.set_attribute("messageID", response["MessageId"])
      return {
         'statusCode': 200,
         'body': json.dumps(response)
      }

   
