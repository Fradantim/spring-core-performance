package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JMeterResult(@JsonProperty("Total") Total total) implements Comparable<JMeterResult> {
	@Override
	public int compareTo(JMeterResult o) {
		return total.compareTo(o.total());
	}
	
	public record Total(Integer sampleCount, Integer throughput, Integer errorCount) implements Comparable<Total> {
		@Override
		public int compareTo(Total o) {
			return throughput.compareTo(o.throughput);
		}
	}
}

//
//{
//	  "HTTP GET Request" : {
//	    "transaction" : "HTTP GET Request",
//	    "sampleCount" : 378407,
//	    "errorCount" : 0,
//	    "errorPct" : 0.0,
//	    "meanResTime" : 13.8883477314109,
//	    "medianResTime" : 0.0,
//	    "minResTime" : 0.0,
//	    "maxResTime" : 593.0,
//	    "pct1ResTime" : 92.0,
//	    "pct2ResTime" : 99.0,
//	    "pct3ResTime" : 181.0,
//	    "throughput" : 6304.681772742419,
//	    "receivedKBytesPerSec" : 1512.8370458545066,
//	    "sentKBytesPerSec" : 744.9868110369459
//	  },
//	  "Total" : {
//	    "transaction" : "Total",
//	    "sampleCount" : 378407,
//	    "errorCount" : 0,
//	    "errorPct" : 0.0,
//	    "meanResTime" : 13.8883477314109,
//	    "medianResTime" : 0.0,
//	    "minResTime" : 0.0,
//	    "maxResTime" : 593.0,
//	    "pct1ResTime" : 92.0,
//	    "pct2ResTime" : 99.0,
//	    "pct3ResTime" : 181.0,
//	    "throughput" : 6304.681772742419,
//	    "receivedKBytesPerSec" : 1512.8370458545066,
//	    "sentKBytesPerSec" : 744.9868110369459
//	  }
//	}