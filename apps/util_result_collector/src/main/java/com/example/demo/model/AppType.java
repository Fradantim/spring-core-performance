package com.example.demo.model;

public enum AppType {
	SIMPLE("Simple web-app"), PSQL("PostgreSQL integrated web-app"), MONGO("MongoDB integrated web-app"),
	REDIS("Redis integrated web-app"), HTTP("Http integrated web-app");

	public final String title;

	private AppType(String title) {
		this.title = title;
	}

	public static AppType get(String appName) {
		if (appName.contains("mongo"))
			return MONGO;
		if (appName.contains("dbc"))
			return PSQL;
		if (appName.contains("redis"))
			return REDIS;
		if (appName.contains("http"))
			return HTTP;
		return SIMPLE;
	}
}
