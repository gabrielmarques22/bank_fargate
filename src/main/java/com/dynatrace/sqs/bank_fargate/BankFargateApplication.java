package com.dynatrace.sqs.bank_fargate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import java.util.Date;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import com.google.gson.Gson;
//import com.googlecode.json-simple;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import java.util.Arrays;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.api.GlobalOpenTelemetry;

import org.json.simple.parser.ParseException;

@SpringBootApplication
public class BankFargateApplication {
	Tracer tracer = GlobalOpenTelemetry
			.getTracerProvider()
			.tracerBuilder("DynaBuilder") // TODO Replace with the name of your tracer
			.build();

	public static void main(String[] args) {
		SpringApplication.run(BankFargateApplication.class, args);
	}

	public class StringToHexadecimal {
		void StringToHexadecimal() {
		}

		String getValue(String integer) {
			StringBuffer sb = new StringBuffer();
			// Converting string to character array
			char ch[] = integer.toCharArray();
			// System.out.println("Converted: "+
			// Long.toHexString(Integer.parseInt(integer)));
			System.out.println("Converted: " + String.format("%x", integer));
			for (int i = 0; i < ch.length; i++) {
				String hexString = Integer.toHexString(ch[i]);

				System.out.println("ch[i]: " + ch[i]);
				System.out.println("hexString: " + hexString);
				sb.append(hexString);
			}
			System.out.println("Hex Conversion: " + sb.toString());
			return (sb.toString());
		}
	}

	public void processMessage(ReceiveMessageRequest receiveRequest, SqsClient sqsClient) throws Exception {
		List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

		// Print out the messages
		if (messages.size() == 0) {
			throw new Exception("Sem mensagens na fila");
		}
		for (Message m : messages) {
			// System.out.println("\n" +m.body());
			// Gson gson = new Gson();
			// Student s = gson.fromJson(jsonString, Student.class)

			//Span span_msg = tracer.spanBuilder("sqs read msg").startSpan();

			try {
				//span_msg.setAttribute("key-2", "value-2");
				StringToHexadecimal hexConversion = new StringToHexadecimal();
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(m.body());
				JSONObject mattrib = (JSONObject) json.get("MessageAttributes");
				System.out.println("mattrib: " + mattrib.toString());
				JSONObject traceid = (JSONObject) mattrib.get("trace_id");
				System.out.println("traceid: " + traceid.toString());
				JSONObject spanid = (JSONObject) mattrib.get("span_id");
				System.out.println("span_id: " + spanid.toString());
				String traceid_value = (String) traceid.get("Value");
				System.out.println("traceid_value: " + traceid_value);
				// hexConversion.getValue(traceid_value);
				String spanid_value = (String) spanid.get("Value");
				System.out.println("spanid_value: " + spanid_value);
				// hexConversion.getValue(spanid_value);
				// openTelemetry.getTracer().get
				// Context remote_ctx = new Context()
				// Context.current().with
				TraceFlags trc_flgs = TraceFlags.fromByte((byte) 0x01);
				// SpanContext.createFromRemoteParent​(traceid_value, spanid_value, trc_flgs,
				// TraceState.builder().build())
				SpanContext span_ctx = SpanContext.createFromRemoteParent(traceid_value, spanid_value,
										trc_flgs, TraceState.builder().build());
								
				try(Scope newScope = Context.current().with(Span.wrap(span_ctx)).makeCurrent()) {
					System.out.println("Span ctx: " + span_ctx);

				// Span span_process_msg = tracer.spanBuilder("sqs process msg").startSpan();
				//Span span_process_msg = Span.wrap(span_ctx);
				Span span_process_msg = tracer.spanBuilder("process message")
					//.setParent(Context.current().with(Span.wrap(span_ctx)))
					.setSpanKind(SpanKind.SERVER)
					.startSpan();

					System.out.println("Span process msg:" + span_process_msg.getSpanContext());
					span_process_msg.end();
				}
			} catch (NullPointerException e) {
				System.out.println("Encontrado valor nulo.");
			} catch (ParseException er) {
				System.out.println("Problema no parse JSON.");
			}
		}

	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			/*
			 * SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
			 * .addSpanProcessor(BatchSpanProcessor.builder().build())
			 * .build();
			 * 
			 * OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
			 * .setTracerProvider(sdkTracerProvider)
			 * //.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.
			 * getInstance()))
			 * .buildAndRegisterGlobal();
			 */

			// Tracer tracer =
			// openTelemetry.getTracer("instrumentation-library-name", "1.0.0");

			System.out.println("no span:" + Context.current());

			SqsClient sqsClient = SqsClient.builder()
					.region(Region.US_EAST_1)
					.build();
			System.out.println("Built client");
			try {
				ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
						.queueUrl(System.getenv("QUEUE_URL"))
						.build();
				System.out.println("Built receive request");

				while (true) {
					try {
						// Span span = tracer.spanBuilder("while_loop").startSpan();
						// span.SpanContext.makeCurrent();
						System.out.println("my span:" + Context.current());
						// put the span into the current Context
						processMessage(receiveRequest, sqsClient);
						Thread.sleep(100);
						// span.end();
					} catch (Exception err) {
						System.out.println("Exceção");
					} finally {
						System.out.println("terminou...");
					}
				}

			} catch (QueueNameExistsException e) {
				throw e;
			}

			/*
			 * System.out.println("Let's inspect the beans provided by Spring Boot:");
			 * 
			 * String[] beanNames = ctx.getBeanDefinitionNames();
			 * Arrays.sort(beanNames);
			 * for (String beanName : beanNames) {
			 * System.out.println(beanName);
			 * }
			 */

		};
	}

}
