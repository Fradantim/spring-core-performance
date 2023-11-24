package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class WebFluxHttpApplication {
	static final Map<Long, Quote> quotes = List.of(
			new Quote(1L,
					"There are only two kinds of languages: the ones people complain about and the ones nobody uses.",
					"Bjarne Stroustrup"),
			new Quote(2L,
					"Any fool can write code that a computer can understand. Good programmers write code that humans can understand.",
					"Martin Fowler"),
			new Quote(3L, "First, solve the problem. Then, write the code.", "John Johnson"),
			new Quote(4L, "Java is to JavaScript what car is to Carpet.", "Chris Heilmann"),
			new Quote(5L,
					"Always code as if the guy who ends up maintaining your code will be a violent psychopath who knows where you live.",
					"John Woods"),
			new Quote(6L, "I'm not a great programmer; I''m just a good programmer with great habits.", "Kent Beck"),
			new Quote(7L, "Truth can only be found in one place: the code.", "Robert C. Martin"),
			new Quote(8L,
					"If you have to spend effort looking at a fragment of code and figuring out what it''s doing, then you should extract it into a function and name the function after the \"what\".",
					"Martin Fowler"),
			new Quote(9L,
					"The real problem is that programmers have spent far too much time worrying about efficiency in the wrong places and at the wrong times; premature optimization is the root of all evil new Quote(or at least most of it) in programming.",
					"Donald Knuth"),
			new Quote(10L,
					"SQL, Lisp, and Haskell are the only programming languages that I’ve seen where one spends more time thinking than typing.",
					"Philip Greenspun"),
			new Quote(11L, "Deleted code is debugged code.", "Jeff Sickel"),
			new Quote(12L,
					"There are two ways of constructing a software design: One way is to make it so simple that there are obviously no deficiencies and the other way is to make it so complicated that there are no obvious deficiencies.",
					"C.A.R. Hoare"),
			new Quote(13L, "Simplicity is prerequisite for reliability.", "Edsger W. Dijkstra"),
			new Quote(14L, "There are only two hard things in Computer Science: cache invalidation and naming things.",
					"Phil Karlton"),
			new Quote(15L,
					"Measuring programming progress by lines of code is like measuring aircraft building progress by weight.",
					"Bill Gates"),
			new Quote(16L, "Controlling complexity is the essence of computer programming.", "Brian Kernighan"),
			new Quote(17L, "The only way to learn a new programming language is by writing programs in it.",
					"Dennis Ritchie"))
			.stream().collect(Collectors.toMap(Quote::id, Function.identity()));

	static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication.run(WebFluxHttpApplication.class, args);
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

	@Bean
	public WebClient webclient(WebClient.Builder builder) {
		return builder.build();
	}
}

@RestController
class QuoteResource {
	@Value("http://localhost:${server.port:8080}")
	private String url;

	@Autowired
	private WebClient wc;

	@GetMapping("/quote/{id}")
	public Mono<Quote> findById(@PathVariable Long id) {
		return wc.get().uri(url + "/inner/quote/" + id).exchangeToMono(c -> c.bodyToMono(Quote.class));
	}

	@GetMapping("/inner/quote/{id}")
	public Mono<Quote> innerFindById(@PathVariable Long id) {
		return Mono.just(WebFluxHttpApplication.quotes.get(id));
	}
}

record Quote(Long id, String quote, String author) {
}