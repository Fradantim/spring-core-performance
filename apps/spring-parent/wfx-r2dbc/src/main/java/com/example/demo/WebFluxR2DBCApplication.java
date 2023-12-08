package com.example.demo;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class WebFluxR2DBCApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebFluxR2DBCApplication.class, args);
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
	private DatabaseClient databaseClient;

	BiFunction<Row, RowMetadata, Quote> quoteRowMapper = (r, rm) -> new Quote(r.get("id", Long.class),
			r.get("quote", String.class), r.get("author", String.class));

	@GetMapping("/quote")
	public Mono<List<Quote>> findAll() {
		return databaseClient.sql("select * from quote").map(quoteRowMapper).all().collectList();
	}

	@GetMapping("/quote/{id}")
	public Mono<ResponseEntity<Quote>> findById(@PathVariable Long id) {
		return wrapOrNotFound(
				databaseClient.sql("select * from quote where id = :id").bind("id", id).map(quoteRowMapper).one());
	}

	public static <S> Mono<ResponseEntity<S>> wrapOrNotFound(Mono<S> maybeResponse) {
		return maybeResponse.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
				.map(response -> ResponseEntity.ok().body(response));
	}
}

record Quote(Long id, String quote, String author) {
}