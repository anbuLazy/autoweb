package com.moto.common.ws.automation.driver;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.skyscreamer.jsonassert.FieldComparisonFailure;

import wslite.http.HTTPResponse;

/**
 * Stores data as a list to be used in the final report.
 */
public class OutputData {
	
	private static final Logger logger = LogManager.getLogger(OutputData.class);
	
	private static Integer callNumber = 1;
	
	private static final List<List> outputData = new ArrayList<List>();
	
	/**
	 * Store data to be used in the final report.
	 * 
	 * @param scenario Scenario List
	 * @param failureCount Failure count List
	 * @param errorContainer Error Container List
	 * @param responseTime Response time List
	 * @param expectedStatus Expected Status List
	 */
	public static void addToDataStorage (final List scenario, final List failureCount, final List errorContainer, final List responseTime, final List expectedStatus) {
		outputData.add(scenario);
		outputData.add(failureCount);
		outputData.add(errorContainer);
		outputData.add(responseTime);
		outputData.add(expectedStatus);
	}
	
	/**
	 * Retrieve stored data.
	 * 
	 * @return Stored data.
	 */
	public static List<List> processStatistics() {
		return outputData;
	}
	
	/**
	 * Store data from a failed test.
	 * 
	 * @param testNumber Test Number 
	 * @param expectedStatusCode expected HTTP status code
	 * @param errorInfo Error information
	 * @param responseTime Response time
	 * @param url URL for the test
	 * @param httpMethod HTTP method for the test.
	 */
	public static void processError(final String testNumber, final String expectedStatusCode, final List<FieldComparisonFailure> errorInfo, final double responseTime, final String url, final String httpMethod) {
		final List errorContainer = new ArrayList();
		final int j = errorInfo.size();
		for (int i = 0; i < errorInfo.size(); i++) {
			errorContainer.add(errorInfo.get(i).getField());
			errorContainer.add(errorInfo.get(i).getExpected());
			errorContainer.add(errorInfo.get(i).getActual());
		}
		if (j == 0) {
			logger.info("Test number " + testNumber + " passed.\n");
		} else {
			logger.error("Test number " + testNumber + " failed. \n");
		}
		
		final List scenarioNumber = new ArrayList();
		scenarioNumber.add(callNumber + "(" + testNumber + ")");
		scenarioNumber.add(url);
		scenarioNumber.add(httpMethod);
		synchronized(callNumber) {
			callNumber++;
		}
		
		final List failureCount = new ArrayList(); 
		failureCount.add(j);

		final List respTime = new ArrayList(); 
		respTime.add(responseTime);
		
		final List expectedStatus = new ArrayList();
		expectedStatus.add(expectedStatusCode);
		
		addToDataStorage(scenarioNumber, failureCount, errorContainer, respTime, expectedStatus);
	}
	
	/**
	 * Store information for a failed test when using a custom validator.
	 * 
	 * @param testNumber Test Number
	 * @param expectedStatusCode Expected HTTP status code
	 * @param jsonData JSON Body Data
	 * @param functionValidator Function Validator Name
	 * @param responseTime Repsonse time
	 * @param url URL used for the test
	 * @param httpMethod HTTP method used for the test.
	 */
	public static void processErrorFunctionValidation(final String testNumber, final String expectedStatusCode, final Object jsonData, final String functionValidator, final double responseTime, final String url, final String httpMethod) {
		final List failures = new ArrayList(); 
		failures.add("Function nValidation");
		failures.add(functionValidator);
		failures.add(jsonData);
		
		logger.error("Test number " + testNumber + " failed. \n");
		
		final List scenarioNumber = new ArrayList();
		scenarioNumber.add(callNumber + "(" + testNumber + ")");
		scenarioNumber.add(url);
		scenarioNumber.add(httpMethod);
		
		synchronized(callNumber) {
			callNumber++;
		}
		
		final List failureCount = new ArrayList(); 
		failureCount.add(1);
		
		final List respTime = new ArrayList(); 
		respTime.add(responseTime);
		
		final List expectedStatus = new ArrayList();
		expectedStatus.add(expectedStatusCode);
		
		addToDataStorage(scenarioNumber, failureCount, failures, respTime, expectedStatus);
	}
	
	/**
	 * Store data for a test which passed.
	 *
	 * @param testNumber Test Number
	 * @param expectedStatusCode Expected HTTP status code.
	 * @param respTime Response Time
	 * @param url URL used for the test
	 * @param httpMethod HTTP method used for the test
	 */
	public static void processPass(final String testNumber, final String expectedStatusCode, final double respTime, final String url, final String httpMethod) {
		final List scenarioNumber = new ArrayList(); 
		scenarioNumber.add(callNumber + "(" + testNumber + ")");
		scenarioNumber.add(url);
		scenarioNumber.add(httpMethod);
		
		final List failureCount = new ArrayList(); 
		failureCount.add(0);
		
		final List errorContainer = new ArrayList();
		
		final List responseTime = new ArrayList();
		responseTime.add(respTime);
		
		final List expectedStatus = new ArrayList(); 
		expectedStatus.add(expectedStatusCode);
		
		addToDataStorage(scenarioNumber, failureCount, errorContainer, responseTime, expectedStatus);
	}
	
	/**
	 * Store test result which failed due to HTTP status code comparison.
	 * 
	 * @param testNumber Test Number
	 * @param expectedStatusCode Expected HTTP status code
	 * @param jsonData JSON Data
	 * @param responseTime Response Time
	 * @param url URL used for the test
	 * @param httpMethod HTTP method used for the test
	 */
	public static void processStatusCodeFailure(final String testNumber, final String expectedStatusCode, final Object jsonData, final Double responseTime, final String url, final String httpMethod) {
		final List failures = new ArrayList(); 
		failures.add("Status Code");
		failures.add(expectedStatusCode);
		failures.add(jsonData instanceof HTTPResponse ? ((HTTPResponse) jsonData).getStatusCode() : jsonData);
		
		final List scenarioNumber = new ArrayList(); 
		scenarioNumber.add(callNumber + "(" + testNumber + ")");
		scenarioNumber.add(url);
		scenarioNumber.add(httpMethod); //[testNumber, url, httpMethod]
		
		final List failureCount = new ArrayList(); 
		failureCount.add(1);
		
		final List respTime = new ArrayList(); 
		respTime.add(responseTime);
		
		final List expectedStatus = new ArrayList();
		expectedStatus.add(expectedStatusCode);
		addToDataStorage(scenarioNumber, failureCount, failures, respTime, expectedStatus);
	}
}