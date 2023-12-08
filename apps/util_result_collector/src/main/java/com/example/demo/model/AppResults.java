package com.example.demo.model;

public class AppResults implements Comparable<AppResults> {

	private App app;
	
	private String folderName;

	private JMeterResult jMeterResult;

	private Integer duration, rampUp;

	private Long epoch;

	@Override
	public int compareTo(AppResults o) {
		return jMeterResult.compareTo(o.jMeterResult);
	}

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public JMeterResult getjMeterResult() {
		return jMeterResult;
	}

	public void setjMeterResult(JMeterResult jMeterResult) {
		this.jMeterResult = jMeterResult;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public Integer getRampUp() {
		return rampUp;
	}

	public void setRampUp(Integer rampUp) {
		this.rampUp = rampUp;
	}

	public Long getEpoch() {
		return epoch;
	}

	public void setEpoch(Long epoch) {
		this.epoch = epoch;
	}
}
