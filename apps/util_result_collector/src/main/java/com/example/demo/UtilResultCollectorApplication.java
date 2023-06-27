package com.example.demo;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class UtilResultCollectorApplication implements CommandLineRunner {

	private static final ObjectMapper om = new ObjectMapper();

	@Value("${PATH_2_LOOK:../../outputs}")
	private String path;

	public static void main(String[] args) {
		SpringApplication.run(UtilResultCollectorApplication.class, args);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void run(String... args) throws Exception {
		Map<String, Map<Integer, Integer>> simpleAppsResults = new TreeMap<>();
		Map<String, Map<Integer, Integer>> dbAppsResults = new TreeMap<>();
		Map<String, Map<Integer, Integer>> mongoAppsResults = new TreeMap<>();

		Stream.of(new File(path).listFiles()).filter(File::isDirectory).peek(f -> System.out.println(f.getName()))
				.flatMap(f -> Stream.of(f.listFiles())).filter(File::isDirectory)
				.peek(f -> System.out.println("\t" + f.getName())).forEach(appTestOutput -> {

					Stream.of(appTestOutput.listFiles()).filter(File::isDirectory)
							.filter(f -> "report".equals(f.getName())).flatMap(f -> Stream.of(f.listFiles()))
							.filter(File::isFile).filter(f -> "statistics.json".equals(f.getName())).findFirst()
							.ifPresent(statFile -> {
								int testValue = 0;
								try {
									Map fileContent = om.readValue(statFile, Map.class);
									testValue = Integer
											.parseInt(((Map) fileContent.get("Total")).get("sampleCount").toString());
								} catch (Exception e) {
									// no-op copntinue
								}

								if (testValue == 0) {
									return;
								}

								Map<String, Map<Integer, Integer>> appsResultByAmountOfThreads;
								if (appTestOutput.getName().contains("mongo")) {
									appsResultByAmountOfThreads = mongoAppsResults;
								} else if (appTestOutput.getName().contains("dbc")) {
									appsResultByAmountOfThreads = dbAppsResults;
								} else {
									appsResultByAmountOfThreads = simpleAppsResults;
								}

								String namePieces[] = appTestOutput.getName().split("_");
								int threads = Integer.parseInt(namePieces[namePieces.length - 1]);
								String tag = namePieces[namePieces.length - 2];
								String appName = String.join("_", Arrays.copyOf(namePieces, namePieces.length - 2));
								System.out.println("\t" + appName + ":" + tag + "@" + threads);

								String appntag = appName + ":" + tag;

								Map<Integer, Integer> bestResultByAmountOfThreads = appsResultByAmountOfThreads
										.get(appntag); // (for this amount of threads)
								if (bestResultByAmountOfThreads == null) {
									bestResultByAmountOfThreads = new TreeMap<>();
									appsResultByAmountOfThreads.put(appntag, bestResultByAmountOfThreads);
								}

								if (bestResultByAmountOfThreads.get(threads) == null
										|| bestResultByAmountOfThreads.get(threads) < testValue) {
									bestResultByAmountOfThreads.put(threads, testValue);
								}
							});
				});

		System.out.println("simple " + simpleAppsResults);
		System.out.println("db " + dbAppsResults);
		System.out.println("mongo " + mongoAppsResults);

		System.out.println();
		System.out.println("### Simple");
		printTable(simpleAppsResults);

		System.out.println();
		System.out.println("### PostgreSQL");
		printTable(dbAppsResults);

		System.out.println();
		System.out.println("### MongoDB");
		printTable(mongoAppsResults);
	}

	private void printTable(Map<String, Map<Integer, Integer>> content) {
		Set<Integer> columnNames = new TreeSet<>(
				content.values().stream().flatMap(v -> v.keySet().stream()).collect(Collectors.toSet()));

		// column titles
		System.out.print("| app & type \\ #clients |");
		columnNames.forEach(c -> System.out.print(" " + c + " |"));
		System.out.println();
		System.out.print("| - |");
		columnNames.forEach(c -> System.out.print(" - |"));
		System.out.println();

		content.forEach((k, v) -> {
			System.out.print("| " + k + " |");
			columnNames.forEach(c -> System.out.print(" " + v.get(c) + " |"));
			System.out.println();
		});
	}
}
