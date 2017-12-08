package com.moto.common.ws.automation.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.moto.common.ws.automation.client.RestClientUtility;

public class PublicUtility {
	private static final Logger logger = LogManager.getLogger(PublicUtility.class);
	private static final String AUTO_MOTO_SERVICE_URL = System.getProperty("auto-moto-services-url");
	//private static final String AUTO_MOTO_SERVICE_URL = System.getProperty("auto-moto-services-url", "http://localhost:8080/auto-moto-services");
	
	public static JSONObject retrieveJSONBasedOnFileName(final String fileName) {
		if (StringUtils.isNotEmpty(fileName)) {
			final File file = new File(fileName);
			if (file.exists()) {
				try (final FileInputStream fis = new FileInputStream(fileName);
						final InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
					return (JSONObject) new JSONParser().parse(isr);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static Object retrieveJSONFromString(final String json) {
		if (StringUtils.isNotEmpty(json)) {
			try {
				if (json.trim().startsWith("{")) {
					return (JSONObject) new JSONParser().parse(json);
				} else if (json.trim().startsWith("[")) {
					return (JSONArray) new JSONParser().parse(json);
				}else{
					return json;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/*public Object retrieveJSONFromStringNew(final String json) {
		if (StringUtils.isNotEmpty(json)) {
			try {
				if (json.trim().startsWith("{")) {
					return (JSONObject) new JSONParser().parse(json);
				} else if (json.trim().startsWith("[")) {
					return (JSONArray) new JSONParser().parse(json);
				}else{
					return json;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}*/
	
	public static String retrieveTestScenarioNamesFromDB(final String json) {
		final RestClientUtility restClientUtility = new RestClientUtility(AUTO_MOTO_SERVICE_URL);
		String scenarioNames = null;
		try {
			scenarioNames = restClientUtility.getTestScenarioNames(json);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return scenarioNames;
	}
	
	public static JSONObject retrieveTestDataFromDB(final String json) {
		final RestClientUtility restClientUtility = new RestClientUtility(AUTO_MOTO_SERVICE_URL);
		String scenarios = null;
		try {
			scenarios = restClientUtility.getTestData(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (StringUtils.isEmpty(scenarios)) {
			logger.error("No test found from DB.");
			return null;
		}
		
		final JSONObject jsonObj = (JSONObject) retrieveJSONFromString(scenarios);
		if (jsonObj != null) {
			final JSONArray testDataArray = (JSONArray) jsonObj.get("data");
			if (testDataArray != null && testDataArray.size() > 0) {
				return (JSONObject) testDataArray.get(0);
				//final JSONObject testDataJSON = (JSONObject) testDataArray.get(0);
				//final String testData = (String) testDataJSON.get("testData");
				//return retrieveJSONFromString(testData.trim());
			}
		}
		return null;
	}

	/*public JSONObject retrieveTestDataFromDBNew(final String json) {
		final RestClientUtility restClientUtility = new RestClientUtility(AUTO_MOTO_SERVICE_URL);
		String scenarios = null;
		try {
			scenarios = restClientUtility.getTestData(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (StringUtils.isEmpty(scenarios)) {
			logger.error("No test found from DB.");
			return null;
		}
		
		final JSONObject jsonObj = (JSONObject) retrieveJSONFromStringNew(scenarios);
		if (jsonObj != null) {
			final JSONArray testDataArray = (JSONArray) jsonObj.get("data");
			if (testDataArray != null && testDataArray.size() > 0) {
				return (JSONObject) testDataArray.get(0);
			}
		}
		return null;
	}*/

	public static String setTestExecutionResult(final String json) {
		final RestClientUtility restClientUtility = new RestClientUtility(AUTO_MOTO_SERVICE_URL);
		String result = null;
		try {
			result = restClientUtility.setTestResultData(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public String setTestExecutResult(final String json) {
		final RestClientUtility restClientUtility = new RestClientUtility(AUTO_MOTO_SERVICE_URL);
		String result = null;
		try {
			result = restClientUtility.setTestResultData(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static JSONObject getServiceHealthCheckStatus(final String json) {
		final RestClientUtility restClientUtility = new RestClientUtility(AUTO_MOTO_SERVICE_URL);
		try {
			final String result = restClientUtility.getServiceHealthCheckStatus(json);
			return (JSONObject) retrieveJSONFromString(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String setHealthCheckResult(final String json) {
		logger.info("JSON: " + json);
		final RestClientUtility restClientUtility = new RestClientUtility(AUTO_MOTO_SERVICE_URL);
		String result = null;
		try {
			result = restClientUtility.setHealthCheckResultData(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * This method retrieves the requested JSON object from the requested
	 * folder - either the input JSON or the expected JSON.
	 *
	 * @param  properties  The loaded properties file
	 * @param  testNumber  The test scenario number.
	 * @param  jsonType    The specification of Input or Expected JSON.
	 */
	/*private static getInputJSON (properties, String testNumber) {
		File jsonFile = new File(properties.jsonInputFolder + testNumber + properties.jsonExtension)
	
		def inputJson
		def slurper = new JsonSlurper()
		
		if (jsonFile.size() != 0) {
			try {
				inputJson = slurper.parseText(jsonFile.getText())
				return inputJson
			} catch (JsonException e) {
				return e.stackTrace
			}
		} else {
			inputJson = slurper.parseText("{}")
			return inputJson
		}
	}
	
	private static JSONObject getExpectedJSON(properties, String testNumber) {
		String fileName = properties.jsonExpectedFolder + testNumber + properties.jsonExtension
		File file = new File(fileName)
		JSONParser parser = new JSONParser()
		if (file.exists() && fileName.size() != 0) {
			try {
				Object obj = parser.parse(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))
				JSONObject jsonObject = (JSONObject) obj
				return jsonObject
			} catch (Exception e) {
				e.printStackTrace()
			}
		} else {
			println "Expected JSON file does not exist... Returning an empty JSON"
			Object obj = parser.parse("{}")
			JSONObject jsonObject = (JSONObject) obj
			return jsonObject
		}
	}
	
	private static JSONObject retrieveTest(properties, String inputData) {
		String fileName = properties.testFolder + inputData
		File file = new File(fileName)
		JSONParser parser = new JSONParser()
		if (file.exists() && fileName.size() != 0) {
			try {
				Object obj = parser.parse(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))
				JSONObject jsonObject = (JSONObject) obj
				return jsonObject
			} catch (Exception e) {
				e.printStackTrace()
			}
		} else {
			println "Expected JSON file does not exist... Returning an empty JSON"
			Object obj = parser.parse("{}")
			JSONObject jsonObject = (JSONObject) obj
			return jsonObject
		}
	}
	
	private static JSONObject retrieveTest1(String fileName) {
		File file = new File(fileName)
		JSONParser parser = new JSONParser()
		if (file.exists() && fileName.size() != 0) {
			try {
				Object obj = parser.parse(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))
				JSONObject jsonObject = (JSONObject) obj
				return jsonObject
			} catch (Exception e) {
				e.printStackTrace()
			}
		} else {
			println "Expected JSON file does not exist... Returning an empty JSON"
			Object obj = parser.parse("{}")
			JSONObject jsonObject = (JSONObject) obj
			return jsonObject
		}
	}
	
	static JSONObject retrieveTestBasedOnFileName(String fileName) {
		final JSONParser parser = new JSONParser();
		if (StringUtils.isEmpty(fileName)) {
			logger.info("Expected JSON file does not exist... Returning an empty JSON");
			Object obj = null;
			try {
				obj = parser.parse("{}");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return (JSONObject) obj;
		}
		
		JSONObject jsonObject = null;
		final File file = new File(fileName);
		if (file.exists()) {
			try {
				final Object obj = parser.parse(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
				jsonObject = (JSONObject) obj;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return jsonObject;
	}*/
}