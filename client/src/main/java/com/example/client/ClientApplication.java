package com.example.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.web.reactive.client.ObservationWebClientCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class ClientApplication {

	private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);

	@Autowired
	private ObservationWebClientCustomizer webClientCustomizer;

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	public WebClient webClientFromBuilder(WebClient.Builder webClientFromBuilder) { // works: http client request metrics exposed
		webClientCustomizer.customize(webClientFromBuilder);
		return webClientFromBuilder
			.baseUrl("http://localhost:6543")
			.build();	
	}

	@Bean
	CommandLineRunner myCommandLineRunnerWebClient(ObservationRegistry registry, @Qualifier("webClientPlainBuilder") WebClient wc) {
		return args -> {
			commandLineArgsBuilder(wc, "webClient");
		};
	}

	private void commandLineArgsBuilder(WebClient webClientObject, String fromWhichWebClient) {
		Random highCardinalityValues = new Random(); // Simulates potentially large number of values
		String highCardinalityUserId = String.valueOf(highCardinalityValues.nextLong(100_000));
		log.info("from: " + fromWhichWebClient);
		log.info("userId: " + highCardinalityUserId);
		log.info("Will send a request to the server"); // Since we're in an observation scope - this log line will contain tracing MDC entries ...
		JsonNode jsonNodeResponse = null;
		Object response = webClientObject
				.get()
				.uri("http://localhost:7654/user/{userId}", String.class, highCardinalityUserId)
				.retrieve()
				.bodyToMono(String.class)
				.block();
		log.info("Got response [{}]", response); // ... so will this line
	}
}



