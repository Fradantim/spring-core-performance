package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run(String... args) throws Exception {
		Map<String, Map<Integer, Map<String, Object>>> simpleAppsResults = new TreeMap<>();
		Map<String, Map<Integer, Map<String, Object>>> dbAppsResults = new TreeMap<>();
		Map<String, Map<Integer, Map<String, Object>>> mongoAppsResults = new TreeMap<>();

		Stream<File> appTestOutputs = Stream.of(new File(path).listFiles()).filter(File::isDirectory)
				.peek(f -> System.out.println(f.getName())).flatMap(f -> Stream.of(f.listFiles()))
				.filter(File::isDirectory).peek(f -> System.out.println("\t" + f.getName()));

		appTestOutputs.forEach(appTestOutput -> {
			Optional<File> optStatFile = Stream.of(appTestOutput.listFiles()).filter(File::isDirectory)
					.filter(f -> "report".equals(f.getName())).flatMap(f -> Stream.of(f.listFiles()))
					.filter(File::isFile).filter(f -> "statistics.json".equals(f.getName())).findFirst();

			optStatFile.ifPresent(statFile -> {
				Map<String, Object> curFileResult;
				try {
					Map fileContent = om.readValue(statFile, Map.class);
					curFileResult = (Map<String, Object>) fileContent.get("Total");
				} catch (Exception e) {
					return;
				}

				Map<String, Map<Integer, Map<String, Object>>> appsResultByAmountOfThreads;
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

				Map<Integer, Map<String, Object>> bestResultByAmountOfThreads = appsResultByAmountOfThreads
						.get(appntag); // (for this app and tag)
				if (bestResultByAmountOfThreads == null) {
					bestResultByAmountOfThreads = new TreeMap<>();
					appsResultByAmountOfThreads.put(appntag, bestResultByAmountOfThreads);
				}

				Map<String, Object> bestResult = bestResultByAmountOfThreads.get(threads);
				// for this app, tag and amount of threads

				if (bestResult == null
						|| ((Integer) bestResult.get("sampleCount")) < ((Integer) curFileResult.get("sampleCount"))) {
					Optional<File> optLogFile = Stream.of(statFile.getParentFile().getParentFile().listFiles())
							.filter(f -> "JMeter_test_plan.log".equals(f.getName())).filter(File::isFile).findFirst();
					optLogFile.ifPresent(logFile -> {
						String duration = null;
						try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
							String line;
							while ((line = reader.readLine()) != null) {
								if (line.contains("DURATION")) {
									duration = line.split("=")[1];
									curFileResult.put("duration", duration);

									break;
								}
							}
						} catch (Exception e) {
							// no-op continue
							return;
						}
					});
					appsResultByAmountOfThreads.get(appntag).put(threads, curFileResult);
				}
			});
		});

		System.out.println("simple " + simpleAppsResults);
		System.out.println("db " + dbAppsResults);
		System.out.println("mongo " + mongoAppsResults);

		System.out.println();
		System.out.println("### Simple");
		printTable(simpleAppsResults);
		printMermaidGanttGraph("a simple app", simpleAppsResults);

		System.out.println();
		System.out.println("### PostgreSQL");
		printTable(dbAppsResults);
		printMermaidGanttGraph("a postgresql integrated app", dbAppsResults);

		System.out.println();
		System.out.println("### MongoDB");
		printTable(mongoAppsResults);
		printMermaidGanttGraph("a mongodb integrated app", dbAppsResults);
	}

	private void printTable(Map<String, Map<Integer, Map<String, Object>>> content) {
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
			columnNames.forEach(c -> System.out.print(" " + v.get(c).get("sampleCount") + " |"));
			System.out.println();
		});
		System.out.println();
	}

	private void printMermaidGanttGraph(String title, Map<String, Map<Integer, Map<String, Object>>> content) {
		Set<Integer> columnNames = new TreeSet<>(
				content.values().stream().flatMap(v -> v.keySet().stream()).collect(Collectors.toSet()));

		String duration = content.values().stream().flatMap(m -> m.values().stream())
				.map(m -> m.get("duration").toString()).findFirst().orElseGet(() -> "?");

		columnNames.forEach(c -> {
			System.out.println("```mermaid");
			System.out.println("gantt");
			System.out.println("\ttitle Amount of requests processed by " + title + " with up to " + c
					+ " concurrent clients in " + duration + "s");
			System.out.println("\tdateFormat X");
			System.out.println("\taxisFormat %s");
			System.out.println();

			content.forEach((appntag, resultsByAmountOfThreads) -> {
				resultsByAmountOfThreads.forEach((amountOfThreads, result) -> {
					if (amountOfThreads.equals(c)) {
						System.out.println("\tsection " + appntag.replace(":", " ").replace("raal", ""));
						System.out.print("\t" + result.get("sampleCount"));
						if (!result.get("errorCount").equals(0)) {
							System.out.print(" (err " + result.get("errorPct") + "%)");
						}
						System.out.println(":0," + result.get("sampleCount"));
					}
				});
			});
			System.out.println("```");
			System.out.println();
		});
	}
}
