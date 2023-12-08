package com.example.demo;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class WebfluxRedisApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebfluxRedisApplication.class, args);
	}

	@Bean
	// @ConditionalOnProperty("stress") // not currently working with spring-native
	CommandLineRunner stresser(@Value("${stress:false}") Boolean stress) {
		return (args) -> {
			if (stress) {
				RestTemplate rt = new RestTemplate();
				rt.getForEntity("http://localhost:8080/quote/11", String.class);
				System.out.println("stressing...");
				IntStream.range(0, 50000).parallel()
						.forEach(i -> rt.getForEntity("http://localhost:8080/quote/11", String.class));
				System.out.println("closing context...");
				// context.close does not stop the executable when args are used
				Executors.newSingleThreadScheduledExecutor().schedule(() -> System.exit(0), 2, TimeUnit.SECONDS);
			}
		};
	}
}

@RestController
class QuoteResource {
	@Autowired
	private ReactiveRedisTemplate<String, String> redisTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@GetMapping("/quote")
	public Mono<List<Quote>> findAll() {
		return redisTemplate.keys("quote_[0-9]*").flatMap(redisTemplate.opsForValue()::get).map(this::safeMap)
				.collectList();
	}

	@GetMapping("/quote/{id}")
	public Mono<ResponseEntity<Quote>> findById(@PathVariable Long id) {
		return wrapOrNotFound(redisTemplate.opsForValue().get("quote_" + id).map(this::safeMap));
	}

	public static <S> Mono<ResponseEntity<S>> wrapOrNotFound(Mono<S> maybeResponse) {
		return maybeResponse.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
				.map(response -> ResponseEntity.ok().body(response));
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