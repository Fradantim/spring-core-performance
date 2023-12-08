package com.example.demo;

import java.util.Collection;
import java.util.Optional;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WebMongoApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebMongoApplication.class, args);
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
	private MongoTemplate mongoTemplate;

	@GetMapping("/quote")
	public Collection<Quote> findAll() {
		return mongoTemplate.findAll(Quote.class);
	}

	@GetMapping("/quote/{id}")
	public ResponseEntity<Quote> findById(@PathVariable Long id) {
		return ResponseEntity.of(Optional
				.ofNullable(mongoTemplate.findOne(new Query().addCriteria(Criteria.where("id").is(id)), Quote.class)));
	}
}

record Quote(@Id Long id, String quote, String author) {
}