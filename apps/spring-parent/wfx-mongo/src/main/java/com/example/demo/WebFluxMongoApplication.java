package com.example.demo;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class WebFluxMongoApplication {
	static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication.run(WebFluxMongoApplication.class, args);
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
	private ReactiveMongoTemplate mongoTemplate;

	@GetMapping("/quote")
	public Mono<List<Quote>> findAll() {
		return mongoTemplate.findAll(Quote.class).collectList();
	}

	@GetMapping("/quote/{id}")
	public Mono<ResponseEntity<Quote>> findById(@PathVariable Long id) {
		return wrapOrNotFound(mongoTemplate.findOne(new Query().addCriteria(Criteria.where("id").is(id)), Quote.class));
	}

	public static <S> Mono<ResponseEntity<S>> wrapOrNotFound(Mono<S> maybeResponse) {
		return maybeResponse.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
				.map(response -> ResponseEntity.ok().body(response));
	}
}

record Quote(@Id Long id, String quote, String author) {
}