package com.moto.common.ws.automation.utility;

import static org.junit.Assert.fail
import groovy.json.*

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.simple.JSONArray
import org.skyscreamer.jsonassert.FieldComparisonFailure
import org.skyscreamer.jsonassert.JSONCompareResult

import wslite.http.HTTPResponse
import wslite.rest.*

import com.moto.common.ws.automation.compare.CompareJSON
import com.moto.common.ws.automation.driver.*

class CompareUtility {
	private static final Logger logger = LogManager.getLogger(CompareUtility.class);
	
	private static List<List> outputData = new ArrayList<List>()
	//[Scenario Number|URL|HTTPMETHOD] | Failures | List of Errors [Field | Expected | Actual] | Response Time | Expected Status Code
	
	/** 
	* This method compares the response JSON with the expected result JSON
	* that was placed in the expectedJson folder.
	*
	* @param  json 		  The response JSON from the API call
	* @param  testNumber  The test scenario number. Used here to retrieve the expected JSON object.
	*/
	static boolean compareJson (def responseMap, String testNumber, Object expectedJSON, String statusCode, String url, String httpMethod, JSONArray includeFields, JSONArray excludeFields) {
		if (statusCode == null || statusCode.trim().empty) {
			return true;
		}
		logger.info "expectedJSON.text : " + expectedJSON
		def response = responseMap.get("response")
		if (response instanceof Response) {
			response = response.getResponse()
		}
		if (statusCode.startsWith("2")) {
			return positiveScenarioCompare(response, testNumber, useExpectedJSON(expectedJSON), statusCode, responseMap.get("responseTime"), url, httpMethod, includeFields, excludeFields)
		} else {
			return negativeScenarioCompareExpected(response, testNumber, useExpectedJSON(expectedJSON), statusCode, responseMap.get("responseTime"), url, httpMethod, includeFields, excludeFields)
		}
	}
	
	private static boolean compareOnlyStatusCode(def jsonData, String testNumber, String expectedStatusCode, double responseTime, String url, String httpMethod) {
		if (jsonData instanceof HTTPResponse && jsonData.getStatusCode().equals(NumberUtils.toInt(expectedStatusCode))) {
			logger.info "Test number " + testNumber + " passed. \n"
			//OutputData.processPass(testNumber, expectedStatusCode, responseTime, url, httpMethod)
			return true;
		} else {
			logger.error("Test number " + testNumber + " failed.\nExpected Status Code: " + expectedStatusCode + ". Actual Status Code: " + jsonData);
			OutputData.processStatusCodeFailure(testNumber, expectedStatusCode, jsonData, responseTime, url, httpMethod)
			return false;
		}
	}
	
	private static boolean positiveScenarioCompare(def jsonData, String testNumber, Object expectedJson, String expectedStatusCode, double responseTime, String url, String httpMethod, JSONArray includeFields, JSONArray excludeFields) {
		if (expectedJson == null) {
			return compareOnlyStatusCode(jsonData, testNumber, expectedStatusCode, responseTime, url, httpMethod)
		}
		boolean test = true
		int jsonFieldPassCount = 0; int jsonFieldFailCount = 0; int jsonFieldTotalCount = 0
		try {
			if (jsonData instanceof HTTPResponse && NumberUtils.toInt(expectedStatusCode).equals(jsonData.getStatusCode())) {
				
				/*Object expectJson = null;
				if (expectedJson && expectedJson instanceof org.json.simple.JSONObject) {
					expectJson = new JSONObject(((org.json.simple.JSONObject)expectedJson).toJSONString());
				} else if (expectedJson && expectedJson instanceof org.json.simple.JSONArray) {
					expectJson = new JSONArray(((org.json.simple.JSONArray)expectedJson).toJSONString());
				}
				
				Object json = null;
				String jsonText = jsonData.text;
				if (jsonText && jsonText.trim().startsWith("{")) {
					json = new JSONObject(jsonText);
				} else if (jsonText && jsonText.trim().startsWith("[")) {
					json = new JSONArray(jsonText);
				}*/
				
				boolean isTestCasePassed = false;
				final JSONCompareResult results = new JSONCompareResult();
				String body = jsonData.getContentAsString();
				if (expectedJson instanceof String) {
					isTestCasePassed = StringUtils.equals("\"" + expectedJson + "\"", body);
					if (!isTestCasePassed) {
						results.fail("Expected Result", expectedJson, body)
					}
				} else if (isJSONValid(expectedJson.toString()) && isJSONValid(body)){
					//JSONCompareResult results = JSONCompare.compareJSON(expectJson, json, JSONCompareMode.LENIENT)
					final CompareJSON compare = new CompareJSON(expectedJson.toString(), body);
					compare.include(includeFields);
					compare.exclude(excludeFields);
					results = compare.compare();
					isTestCasePassed = !results.failed();
				}

				if (isTestCasePassed) {
					logger.info "Test number " + testNumber + " passed. \n"
					return true;
				} else if (StringUtils.isBlank(body) && NumberUtils.toInt(expectedStatusCode).equals(jsonData.getStatusCode())) {
					logger.error("Test number " + testNumber + " failed.\nExpected Status Code: " + expectedStatusCode + ". Actual Status Code: " + jsonData.getStatusCode() + "\n");
					OutputData.processError(testNumber, expectedStatusCode, Arrays.asList(new FieldComparisonFailure("Response Body", expectedJson.toString(), "None")), responseTime, url, httpMethod);
					return false;
				} else {
					final List<FieldComparisonFailure> failures = new ArrayList<FieldComparisonFailure>();
					if (CollectionUtils.isNotEmpty(results.getFieldFailures())) {
						CollectionUtils.addAll(failures, results.getFieldFailures());
					}
					if (CollectionUtils.isNotEmpty(results.getFieldMissing())) {
						CollectionUtils.addAll(failures, results.getFieldMissing());
					}
					if (CollectionUtils.isNotEmpty(results.getFieldUnexpected())) {
						CollectionUtils.addAll(failures, results.getFieldUnexpected());
					}
					OutputData.processError(testNumber, expectedStatusCode, failures, responseTime, url, httpMethod)
					return false;
				}
			} else {
				logger.error("Test number " + testNumber + " failed.\nExpected Status Code: " + expectedStatusCode + ". Actual Status Code: " + jsonData + "\n");
				OutputData.processStatusCodeFailure(testNumber, expectedStatusCode, jsonData, responseTime, url, httpMethod)
				return false;
			}
		} catch (Exception e) {
			logger.error "***Error*** :::: " + e.printStackTrace()
			return false;
		}
	}
	
	private static boolean negativeScenarioCompare(def json, String testNumber, String expectedStatusCode, double responseTime, String url, String httpMethod) {
		boolean isTestCasePassed = false;
		if (json instanceof String) {
			if (json.contains(expectedStatusCode)) {
				isTestCasePassed = true
			}
		} else if (json instanceof HTTPResponse && ((HTTPResponse) json).getStatusCode().equals(NumberUtils.toInt(expectedStatusCode))) {
			isTestCasePassed = true
		}
		
		if (isTestCasePassed) {
			logger.info "[Negative Scenario] Test " + testNumber + " has passed."
			logger.info "Expected Status: " + expectedStatusCode + ". Actual Status: " + json + "\n"
		} else {
			logger.error("Test " + testNumber + " has failed.");
			logger.error "Expected Status: " + expectedStatusCode + ". Actual Status: " + json + "\n"
			OutputData.processStatusCodeFailure(testNumber, expectedStatusCode, json, responseTime, url, httpMethod)
		}
		return isTestCasePassed;
	}
	
	private static boolean negativeScenarioCompareExpected(def json, String testNumber, Object expectedJson, String expectedStatusCode, double responseTime, String url, String httpMethod, JSONArray includeFields, JSONArray excludeFields) {
		if (expectedJson == null) {
			return negativeScenarioCompare(json, testNumber, expectedStatusCode, responseTime, url, httpMethod);
		}
		boolean test = true
		int jsonFieldPassCount = 0; int jsonFieldFailCount = 0; int jsonFieldTotalCount = 0
		try {
			String body = json instanceof HTTPResponse ? json.getContentAsString() : "";
			if (StringUtils.isNotBlank(body) && NumberUtils.toInt(expectedStatusCode).equals(json.getStatusCode())) {
				
				boolean isTestCasePassed = false;
				final JSONCompareResult results = new JSONCompareResult();
				
				if (expectedJson instanceof String) {
					isTestCasePassed = StringUtils.equals("\"" + expectedJson + "\"", body);
					if (!isTestCasePassed) {
						results.fail("Expected Result", expectedJson, body)
					}
//				} else {
				} else if (isJSONValid(expectedJson.toString()) && isJSONValid(body)){
					final CompareJSON compare = new CompareJSON(expectedJson.toString(), body);
					compare.include(includeFields);
					compare.exclude(excludeFields);
					results = compare.compare();
					isTestCasePassed = !results.failed();
				}
				
				if (isTestCasePassed) {
					logger.info "[Negative Scenario] Test number " + testNumber + " passed. \n"
					//processPass(testNumber, expectedStatusCode, responseTime, url, httpMethod)
					return true;
				} else {
					final List<FieldComparisonFailure> failures = results.getFieldFailures()
					OutputData.processError(testNumber, expectedStatusCode, failures, responseTime, url, httpMethod);
					return false;
				}
			} else if (StringUtils.isBlank(body) && NumberUtils.toInt(expectedStatusCode).equals(json.getStatusCode())) {
				logger.error("Test number " + testNumber + " failed.\nExpected Status Code: " + expectedStatusCode + ". Actual Status Code: " + json.getStatusCode() + "\n");
				OutputData.processError(testNumber, expectedStatusCode, Arrays.asList(new FieldComparisonFailure("Response Body", expectedJson.toString(), "<empty>")), responseTime, url, httpMethod);
				return false;
			} else {
				logger.error("Test number " + testNumber + " failed.\nExpected Status Code: " + expectedStatusCode + ". Actual Status Code: " + json.getStatusCode() + "\n");
				OutputData.processStatusCodeFailure(testNumber, expectedStatusCode, json, responseTime, url, httpMethod)
				return false;
			}
		} catch (Exception e) {
			logger.error "***Error*** :::: " + e.printStackTrace()
			return false;
		}
		// 
	}
	
	private static Object useExpectedJSON(Object expectedJSON) {
		if (expectedJSON == null || StringUtils.isBlank(expectedJSON.toString()) || expectedJSON.toString().equals("{}") || expectedJSON.toString().equals("[]")) {
			return null;
		} else {
			return expectedJSON;
		}
	}
	
	private static boolean isJSONValid(String test) {
		if (StringUtils.isBlank(test)) {
			return false;
		}
		try {
			if (test.startsWith("{")) {
				net.sf.json.JSONObject.fromObject(test);
			} else if (test.startsWith("[")) {
				net.sf.json.JSONArray.fromObject(test);
			} else {
				return false;
			}
		} catch (net.sf.json.JSONException ex) {
			ex.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}