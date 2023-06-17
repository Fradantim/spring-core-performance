package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class WebFluxRMongoApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebFluxRMongoApplication.class, args);
	}
}

@RestController
class QuoteResource {
	@Autowired
	private QuoteRepository quoteRepository;

	@GetMapping("/quote")
	public Mono<List<Quote>> findAll() {
		return quoteRepository.findAll().collectList();
	}

	@GetMapping("/quote/{id}")
	public Mono<ResponseEntity<Quote>> findById(@PathVariable Long id) {
		return wrapOrNotFound(quoteRepository.findById(id));
	}

	public static <S> Mono<ResponseEntity<S>> wrapOrNotFound(Mono<S> maybeResponse) {
		return maybeResponse.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
				.map(response -> ResponseEntity.ok().body(response));
	}
}

interface QuoteRepository extends ReactiveMongoRepository<Quote, Long> {
}

record Quote(@Id Long id, String quote, String author) {
}