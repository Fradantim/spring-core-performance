package com.example.demo;

import java.util.Collection;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WebHttpApplication {
	static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication.run(WebHttpApplication.class, args);
	}

	@Bean
	// @ConditionalOnProperty("stress") // not currently working with spring-native
	public CommandLineRunner stresser(@Value("${stress:false}") Boolean stress) {
		return (args) -> {
			if (stress) {
				RestTemplate rt = new RestTemplate();
				rt.getForEntity("http://localhost:8080/quote/11", String.class);
				System.out.println("stressing...");
				IntStream.range(0, 50000).parallel().forEach(i -> {
					rt.getForEntity("http://localhost:8080/quote/11", String.class);
				});
				System.out.println("closing context...");
				// context.close does not stop the executable on pgo-instrumented compilation
				// Executors.newSingleThreadScheduledExecutor().schedule(() -> ctx.close(), 2,
				// TimeUnit.SECONDS);
				System.exit(0);
			}
		};
	}
}

@RestController
class QuoteResource {
	private final static RestTemplate rt = new RestTemplate();

	@Value("${url}")
	private String url;

	@GetMapping("/quote")
	public Collection<Quote> findAll() {
		throw new UnsupportedOperationException();
	}

	@GetMapping("/quote/{id}")
	public Quote findById(@PathVariable Long id) {
		return rt.getForEntity(url+"/quote/"+id, Quote.class).getBody();
	}
}

record Quote(Long id, String quote, String author) {
}