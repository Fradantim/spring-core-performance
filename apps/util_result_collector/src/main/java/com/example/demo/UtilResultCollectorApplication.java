package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.model.App;
import com.example.demo.model.AppResults;
import com.example.demo.model.AppType;
import com.example.demo.model.JMeterResult;
import com.example.demo.model.PrometheusResult;
import com.example.demo.model.PrometheusResult.Result;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class UtilResultCollectorApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(UtilResultCollectorApplication.class);

	private static final RestTemplate rt = new RestTemplate();

	private static final Map<App, AppResults> results = new HashMap<>();

	@Value("${path-2-look:../../outputs}")
	private String path;

	@Value("${prometheus.url:http://localhost:9090/api/v1/query_range}")
	private String prometheusUrl;

	@Autowired
	private ObjectMapper om;

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(UtilResultCollectorApplication.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		application.run(args);
	}

	private <T> T readFile(File file, Class<T> type) {
		try {
			return om.readValue(file, type);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private App fromFolderName(File folder) {
		String namePieces[] = folder.getName().split("_");
		int i = 0;
		String app = namePieces[i++];
		String tag = namePieces[i++];
		String threadType = namePieces[i++];
		int cpus = Integer.parseInt(namePieces[i++]);
		int clients = Integer.parseInt(namePieces[i++]);

		return new App(app, tag, threadType, cpus, clients);
	}

	private void fillAllDataFromJMeterFolder(File folder, AppResults appResults) {
		appResults.setFolderName(folder.getName());
		String namePieces[] = folder.getName().split("_");
		String timestampStr = namePieces[namePieces.length - 1];
		timestampStr = timestampStr.substring(0, 4) + "-" + timestampStr.substring(4, 6) + "-"
				+ timestampStr.substring(6, 8) + "T" + timestampStr.substring(8, 10) + ":"
				+ timestampStr.substring(10, 12) + ":" + timestampStr.substring(12, 14) + "Z";

		OffsetDateTime timestamp = OffsetDateTime.parse(timestampStr);
		appResults.setEpoch(timestamp.toInstant().toEpochMilli());

		Optional<File> optLogFile = Stream.of(folder.listFiles())
				.filter(f -> "JMeter_test_plan.log".equals(f.getName())).filter(File::isFile).findFirst();
		optLogFile.ifPresent(logFile -> {
			try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains("DURATION"))
						appResults.setDuration(Integer.parseInt(line.split("=")[1]));
					if (line.contains("RAMP_UP"))
						appResults.setRampUp(Integer.parseInt(line.split("=")[1]));
					if (appResults.getDuration() != null && appResults.getRampUp() != null)
						break;
				}
			} catch (IOException e) {
				logger.error("{}", e.getLocalizedMessage()); // no-op continue
			}
		});
	}

	private Function<AppResults, String> amountProcessedMapper = (r) -> {
		String res = r.getjMeterResult().total().sampleCount().toString();
		if (!r.getjMeterResult().total().errorCount().equals(0)) {
			res += "<br>e: " + r.getjMeterResult().total().errorCount();
		}
		return res;
	};

	private Function<AppResults, String> speedMapper = (r) -> String
			.valueOf(r.getjMeterResult().total().throughput().intValue());

	private Function<AppResults, String> prometheusQuery(String query, BinaryOperator<Double> reductor) {
		return (r) -> {
			long timeDelta = (r.getDuration() + r.getRampUp()) * 2 * 1000;
			long end = (r.getEpoch() + timeDelta) / 1000;
			long start = r.getEpoch() / 1000;
			String url = UriComponentsBuilder.fromHttpUrl(prometheusUrl).queryParam("query", query)
					.queryParam("start", start).queryParam("end", end).queryParam("step", 2) // 2 may be dangerous
					.toUriString();

			PrometheusResult res = rt.getForEntity(url, PrometheusResult.class).getBody();

			if (res.error() != null) {
				logger.error("{} {} {}", r.getFolderName(), query, res.error());
				return "pmtheus_error";
			}

			if (res.data() == null || res.data().result() == null || res.data().result().isEmpty())
				return "pmtheus_empty";

			Stream<Result> thisExecutionResults = res.data().result().stream()
					.filter(pr -> r.getFolderName().equals(pr.metric().application()));

			Map<Object, List<List<Object>>> thisExecutionResultsGroupedByTimestamp = thisExecutionResults
					.map(Result::values).flatMap(List::stream).filter(l -> l.size() > 1)
					.collect(Collectors.groupingBy(l -> l.get(0)));

			String ress = thisExecutionResultsGroupedByTimestamp.values().stream()
					.map(l -> l.stream().filter(ls -> ls.size() > 1).map(ls -> ls.get(1)).map(String.class::cast)
							.map(Double::parseDouble).reduce((a, b) -> a + b).get())
					.reduce(reductor).map(String::valueOf).orElse("N/A");
			return ress;
		};
	}

	private Function<AppResults, String> startUpMapper = prometheusQuery("application_started_time_seconds",
			(a, b) -> a);

	private Function<AppResults, String> cpuUsageAndMemoryAndThreadsMapper = (r) -> {
		String cpuUsage = prometheusQuery("system_cpu_usage", Math::max).apply(r);
		cpuUsage = String.format("%.2f", Double.parseDouble(cpuUsage) * 100);
		String memory = toSize(prometheusQuery("jvm_memory_committed_bytes", Math::max).apply(r));
		String threads = String
				.valueOf(((Double) Double.parseDouble(prometheusQuery("jvm_threads_peak_threads", Math::max).apply(r)))
						.intValue());
		return cpuUsage + "<br>" + memory + "<br>" + threads;
	};

	private String toSize(String bytes) {
		return toSize(Double.parseDouble(bytes));
	}

	private String toSize(Double bytes) {
		String[] units = new String[] { "", "K", "M", "G", "T", "P" };
		for (String unit : units) {
			if (bytes / 1024 >= 1) {
				bytes /= 1024;
			} else {
				return String.format("%.2f", bytes) + unit + "B";
			}
		}

		return String.format("%.2f", bytes * 1024) + "PB";
	}

	// TODO
	// fix f.getName().startsWith("2")), may stop working after the year 3000
	@Override
	public void run(String... args) throws Exception {
		Stream<File> appTestOutputs = Stream.of(new File(path).listFiles()).filter(File::isDirectory)
				.filter(f -> f.getName().startsWith("2")).peek(f -> System.out.print("\n" + f.getName() + ": "))
				.flatMap(f -> Stream.of(f.listFiles())).filter(File::isDirectory)
				.peek(f -> System.out.print(f.getName() + " "));

		appTestOutputs.forEach(appTestOutput -> {
			Optional<File> optStatFile = Stream.of(appTestOutput.listFiles()).filter(File::isDirectory)
					.filter(f -> "report".equals(f.getName())).flatMap(f -> Stream.of(f.listFiles()))
					.filter(File::isFile).filter(f -> "statistics.json".equals(f.getName())).findFirst();
			optStatFile.ifPresent(statFile -> {
				App app = fromFolderName(appTestOutput);
				AppResults thisResults = new AppResults();
				thisResults.setjMeterResult(readFile(statFile, JMeterResult.class));

				AppResults bestResult = results.get(app);
				if (bestResult == null || thisResults.compareTo(bestResult) > 0) {
					// set or change results
					fillAllDataFromJMeterFolder(appTestOutput, thisResults);
					thisResults.setApp(app);
					results.put(app, thisResults);
				}
			});
		});

		Map<AppType, List<AppResults>> resultsByType = results.values().stream()
				.collect(Collectors.groupingBy(r -> AppType.get(r.getApp().app()), TreeMap::new, Collectors.toList()));

		System.out.println();
		Consumer<String> printer = System.out::print; // TODO send 2 file?

		resultsByType.forEach((type, results) -> {
			printer.accept("### " + type.title + "\n\n");

			String duration = results.stream().map(AppResults::getDuration).filter(Objects::nonNull)
					.map(Object::toString).findFirst().orElseGet(() -> "?");
			String rampUp = results.stream().map(AppResults::getRampUp).filter(Objects::nonNull).map(Object::toString)
					.findFirst().orElseGet(() -> "?");

			printer.accept("Duration: " + duration + "s, ramp up: " + rampUp + "s\n\n");

			printTable(printer, "#### Requests processed per second (JMeter)", false, results, speedMapper);
			printTable(printer, "#### Amount of requests processed (JMeter)", true, results, amountProcessedMapper);
			printTable(printer, "#### Start up in seconds (Prometheus)", true, results, startUpMapper);
			printTable(printer, "#### CPU usage % + memory + peak threads (Prometheus)", true, results,
					cpuUsageAndMemoryAndThreadsMapper);
		});
	}

	private void printTable(Consumer<String> out, String title, boolean hide, List<AppResults> results,
			Function<AppResults, String> resultMapper) {
		Set<Integer> clientss = results.stream().map(r -> r.getApp().clients())
				.collect(Collectors.toCollection(TreeSet::new));
		Set<Integer> cpuss = results.stream().map(r -> r.getApp().cpus())
				.collect(Collectors.toCollection(TreeSet::new));
		Set<String> apps = results.stream().map(r -> r.getApp().app()).collect(Collectors.toCollection(TreeSet::new));

		out.accept(title + "\n");
		if (hide) {
			out.accept("<details>\n");
			out.accept("<summary>Click to expand</summary>\n");
		}
		out.accept("<table>\n");
		// headers
		out.accept("<tr>");
		out.accept("<th></th>"); // app column
		out.accept("<th></th>"); // tag column
		out.accept("<th></th>"); // thread type column
		out.accept("<th colspan=\"" + clientss.size() * cpuss.size() + "\">#clients & #cores</th>");
		out.accept("</tr>\n");

		out.accept("<tr>");
		out.accept("<th></th>"); // app column
		out.accept("<th></th>"); // tag column
		out.accept("<th></th>"); // thread type column
		clientss.forEach(clients -> {
			out.accept("<th colspan=\"" + cpuss.size() + "\">" + clients + "</th>");
		});
		out.accept("</tr>\n");

		out.accept("<tr>");
		out.accept("<th>app</th>");
		out.accept("<th>tag</th>");
		out.accept("<th>thread</th>");
		clientss.forEach(clients -> {
			cpuss.forEach(cpus -> {
				out.accept("<th>" + cpus + "</th>");
			});
		});
		out.accept("</tr>\n");

		// data
		for (String app : apps) {
			Set<String> tags = results.stream().filter(r -> r.getApp().app().equals(app)).map(r -> r.getApp().tag())
					.collect(Collectors.toCollection(TreeSet::new));

			Long appRowSpan = results.stream().filter(r -> r.getApp().app().equals(app))
					.map(r -> r.getApp().app() + "_" + r.getApp().tag() + "_" + r.getApp().threadType()).distinct()
					.count();
			String lastApp = null;
			String lastTag = null;

			for (String tag : tags) {
				Set<String> threadTypes = results.stream()
						.filter(r -> r.getApp().app().equals(app) && r.getApp().tag().equals(tag))
						.map(r -> r.getApp().threadType()).collect(Collectors.toCollection(TreeSet::new));

				Long tagRowSpan = results.stream()
						.filter(r -> r.getApp().app().equals(app) && r.getApp().tag().equals(tag))
						.map(r -> r.getApp().app() + "_" + r.getApp().tag() + "_" + r.getApp().threadType()).distinct()
						.count();

				for (String threadType : threadTypes) {
					out.accept("<tr>");
					boolean newApp = false;
					if (!app.equals(lastApp)) {
						// new app
						newApp = true;
						lastApp = app;
						out.accept("<td rowspan=\"" + appRowSpan + "\">" + app + "</td>");
					}

					if (newApp || !tag.equals(lastTag)) {
						// new tag
						lastTag = tag;
						out.accept("<td rowspan=\"" + tagRowSpan + "\">" + tag + "</td>");
					}

					out.accept("<td>" + threadType + "</td>");

					clientss.forEach(clients -> {
						cpuss.forEach(cpus -> {
							App app2find = new App(app, tag, threadType, cpus, clients);
							String result = results.stream().filter(r -> r.getApp().equals(app2find)).findFirst()
									.map(resultMapper).orElseGet(() -> "N/A");
							out.accept("<th>" + result + "</th>");
						});
					});
					out.accept("</tr>\n");
				}
			}
		}
		out.accept("</table>\n");
		if (hide) {
			out.accept("</details>");
		}
		out.accept("\n\n");
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
