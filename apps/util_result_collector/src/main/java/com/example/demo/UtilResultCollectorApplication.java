package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
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

	record AppResult(String app, String tag, int cpus, int clients, Map<String, Object> result)
			implements Comparable<AppResult> {
		@Override
		public boolean equals(Object o) {
			if (o instanceof AppResult or) {
				return cpus == or.cpus && clients == or.clients && app.equals(or.app) && tag.equals(or.tag);
			}
			return false;
		}

		@Override
		public int compareTo(AppResult o) {
			return ((Integer) result.get("sampleCount")).compareTo(((Integer) o.result.get("sampleCount")));
		}
	}

	@Value("${PATH_2_LOOK:../../outputs}")
	private String path;

	public static void main(String[] args) {
		SpringApplication.run(UtilResultCollectorApplication.class, args);
	}

	static Optional<AppResult> find(List<AppResult> results, AppResult result) {
		return results.stream().filter(r -> r.equals(result)).findFirst();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run(String... args) throws Exception {
		List<AppResult> siAppResults = new ArrayList<>();
		List<AppResult> dbAppResults = new ArrayList<>();
		List<AppResult> mgAppResults = new ArrayList<>();
		List<AppResult> rdAppResults = new ArrayList<>();
		List<AppResult> htAppResults = new ArrayList<>();

		Stream<File> appTestOutputs = Stream.of(new File(path).listFiles()).filter(File::isDirectory)
				.peek(f -> System.out.print(f.getName()+": ")).flatMap(f -> Stream.of(f.listFiles()))
				.filter(File::isDirectory).peek(f -> System.out.print(f.getName()+" "));

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

				List<AppResult> appResults;
				if (appTestOutput.getName().contains("mongo")) {
					appResults = mgAppResults;
				} else if (appTestOutput.getName().contains("dbc")) {
					appResults = dbAppResults;
				} else if (appTestOutput.getName().contains("redis")) {
					appResults = rdAppResults;
				} else if (appTestOutput.getName().contains("http")) {
					appResults = htAppResults;
				} else {
					appResults = siAppResults;
				}

				String namePieces[] = appTestOutput.getName().split("_");
				int clients = Integer.parseInt(namePieces[namePieces.length - 1]);
				int cpus = Integer.parseInt(namePieces[namePieces.length - 2]);
				String tag = namePieces[namePieces.length - 3];
				String appName = String.join("_", Arrays.copyOf(namePieces, namePieces.length - 3));

				AppResult curResult = new AppResult(appName, tag, cpus, clients, curFileResult);

				find(appResults, curResult).ifPresentOrElse((existingResult) -> {
					if (curResult.compareTo(existingResult) > 0) {
						appResults.remove(existingResult);
						appResults.add(curResult);
					}
				}, () -> {
					appResults.add(curResult);
				});

				Optional<File> optLogFile = Stream.of(statFile.getParentFile().getParentFile().listFiles())
						.filter(f -> "JMeter_test_plan.log".equals(f.getName())).filter(File::isFile).findFirst();
				optLogFile.ifPresent(logFile -> {
					try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
						String line;
						boolean duration = false, rampUp = false;
						while ((line = reader.readLine()) != null) {
							if (line.contains("DURATION")) {
								curFileResult.put("duration", line.split("=")[1]);
								duration = true;
							}
							if (line.contains("RAMP_UP")) {
								curFileResult.put("ramp_up", line.split("=")[1]);
								rampUp = true;
							}

							if (duration && rampUp) {
								break;
							}
						}
					} catch (Exception e) {
						// no-op continue
						return;
					}
				});
			});
		});

		Function<AppResult, String> amountProcessedMapper = (r) -> {
			String res = r.result.get("sampleCount").toString();
			if (!r.result.get("errorCount").equals(0)) {
				res += "<br>e: " + r.result.get("errorCount");
			}
			return res;
		};

		Function<AppResult, String> speedMapper = (r) -> String
				.valueOf(((Number) r.result.get("throughput")).intValue());

		System.out.println();
		System.out.println("### Simple app");
		printTable("Requests processed per second", siAppResults, speedMapper);
		printTable("Amount of requests processed", siAppResults, amountProcessedMapper);

		System.out.println();
		System.out.println("### PostgreSQL integrated app");
		printTable("Requests processed per second", dbAppResults, speedMapper);
		printTable("Amount of requests processed", dbAppResults, amountProcessedMapper);

		System.out.println();
		System.out.println("### MongoDB integrated app");
		printTable("Requests processed per second", mgAppResults, speedMapper);
		printTable("Amount of requests processed", mgAppResults, amountProcessedMapper);
		
		System.out.println();
		System.out.println("### Redis integrated app");
		printTable("Requests processed per second", rdAppResults, speedMapper);
		printTable("Amount of requests processed", rdAppResults, amountProcessedMapper);
		
		System.out.println();
		System.out.println("### Http integrated app");
		printTable("Requests processed per second", htAppResults, speedMapper);
		printTable("Amount of requests processed", htAppResults, amountProcessedMapper);
	}

	private void printTable(String title, List<AppResult> results, Function<AppResult, String> resultMapper) {
		String duration = results.stream().filter(r -> r.result.get("duration") != null)
				.map(r -> r.result.get("duration").toString()).findFirst().orElseGet(() -> "?");
		String rampUp = results.stream().filter(r -> r.result.get("ramp_up") != null)
				.map(r -> r.result.get("ramp_up").toString()).findFirst().orElseGet(() -> "?");

		System.out.println(title + " in " + duration + "s with a ramp up of " + rampUp + "s");
		Set<Integer> clientss = results.stream().map(r -> r.clients).collect(Collectors.toCollection(TreeSet::new));
		Set<Integer> cpuss = results.stream().map(r -> r.cpus).collect(Collectors.toCollection(TreeSet::new));
		Set<String> apps = results.stream().map(r -> r.app).collect(Collectors.toCollection(TreeSet::new));

		System.out.println("<table>");
		// headers
		System.out.print("<tr>");
		System.out.print("<th></th>");
		System.out.print("<th></th>");
		System.out.print("<th colspan=\"" + clientss.size() * cpuss.size() + "\">#clients & #cores</th>");
		System.out.println("</tr>");

		System.out.print("<tr>");
		System.out.print("<th></th>");
		System.out.print("<th></th>");
		clientss.forEach(clients -> {
			System.out.print("<th colspan=\"" + cpuss.size() + "\">" + clients + "</th>");
		});
		System.out.println("</tr>");

		System.out.print("<tr>");
		System.out.print("<th>app</th>");
		System.out.print("<th>tag</th>");
		clientss.forEach(clients -> {
			cpuss.forEach(cpus -> {
				System.out.print("<th>" + cpus + "</th>");
			});
		});
		System.out.println("</tr>");

		// data
		apps.forEach(app -> {
			Set<String> tags = results.stream().filter(r -> r.app.equals(app)).map(r -> r.tag)
					.collect(Collectors.toCollection(TreeSet::new));
			AtomicBoolean firstTag = new AtomicBoolean(true);
			tags.forEach(tag -> {
				System.out.print("<tr>");
				if (firstTag.get()) {
					firstTag.set(false);
					System.out.print("<td rowspan=\"" + tags.size() + "\">" + app + "</td>");
				}
				System.out.print("<td>" + tag + "</td>");
				clientss.forEach(clients -> {
					cpuss.forEach(cpus -> {
						AppResult result2find = new AppResult(app, tag, cpus, clients, null);
						String result = results.stream().filter(r -> r.equals(result2find)).findFirst()
								.map(resultMapper).orElseGet(() -> "N/A");
						System.out.print("<th>" + result + "</th>");
					});
				});
				System.out.println("</tr>");
			});
		});
		System.out.println("</table>");
		System.out.println();
	}

	@SuppressWarnings("unused")
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

	/** @deprecated does not read easily in github markdown */
	@Deprecated
	@SuppressWarnings("unused")
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
