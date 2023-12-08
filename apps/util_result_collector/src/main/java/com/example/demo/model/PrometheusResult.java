package com.example.demo.model;

import java.util.List;

public record PrometheusResult(String status, Data data, String error) {
	public record Data(String resultType, List<Result> result) {
	}

	public record Result(Metric metric, List<List<Object>> values) {
	}

	public record Metric(String application) {
	}
}
