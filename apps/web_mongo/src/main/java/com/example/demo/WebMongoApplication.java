package com.example.demo;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class WebMongoApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebMongoApplication.class, args);
	}
}

@RestController
class QuoteResource {

	@Autowired
	private QuoteRepository quoteRepository;

	@GetMapping("/quote")
	public Collection<Quote> findAll() {
		return quoteRepository.findAll();
	}

	@GetMapping("/quote/{id}")
	public ResponseEntity<Quote> findById(@PathVariable Long id) {
		return ResponseEntity.of(quoteRepository.findById(id));
	}
}

interface QuoteRepository extends ListCrudRepository<Quote, Long> {
}

record Quote(@Id Long id, String quote, String author) {
}