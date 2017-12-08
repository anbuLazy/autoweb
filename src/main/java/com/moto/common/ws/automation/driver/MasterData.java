package com.moto.common.ws.automation.driver;

import java.util.Map;

import org.json.simple.JSONObject;

public class MasterData {
	private static JSONObject jsonMasterConfig;
	private static JSONObject jsonConfig;
	private static Map<String, String> urls;
	private static long waitTimeInSecond = 0;

	public static final JSONObject getJsonMasterConfig() {
		return jsonMasterConfig;
	}

	public static final void setJsonMasterConfig(final JSONObject jsonMasterConfig) {
		MasterData.jsonMasterConfig = jsonMasterConfig;
	}

	public static final JSONObject getJsonConfig() {
		return jsonConfig;
	}

	public static final void setJsonConfig(final JSONObject jsonConfig) {
		MasterData.jsonConfig = jsonConfig;
	}

	public static Map<String, String> getUrls() {
		return urls;
	}

	public static void setUrls(final Map<String, String> urls) {
		MasterData.urls = urls;
	}

	public static long getWaitTimeInSecond() {
		return waitTimeInSecond;
	}

	public static void setWaitTimeInSecond(final long waitTimeInSecond) {
		MasterData.waitTimeInSecond = waitTimeInSecond;
	}
}