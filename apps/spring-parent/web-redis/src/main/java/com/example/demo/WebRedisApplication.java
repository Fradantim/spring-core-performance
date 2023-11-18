package com.example.demo;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class WebRedisApplication {
	static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication.run(WebRedisApplication.class, args);
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
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@GetMapping("/quote")
	public Collection<Quote> findAll() {
		return redisTemplate.keys("quote_[0-9]*").stream().map(redisTemplate.opsForValue()::get).map(this::safeMap)
				.toList();
	}

	@GetMapping("/quote/{id}")
	public ResponseEntity<Quote> findById(@PathVariable Long id) {
		return ResponseEntity
				.of(Optional.ofNullable(redisTemplate.opsForValue().get("quote_" + id)).map(this::safeMap));
	}

	private Quote safeMap(String data) {
		try {
			return objectMapper.readValue(data, Quote.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}
}

record Quote(@Id Long id, String quote, String author) {
}