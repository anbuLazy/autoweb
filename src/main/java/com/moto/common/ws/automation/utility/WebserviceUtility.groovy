package com.moto.common.ws.automation.utility;

import java.util.Map;

import groovy.json.*

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import wslite.rest.*

public class WebserviceUtility {
	private static final Logger logger = LogManager.getLogger(WebserviceUtility.class);
	
	/*static httpClient(String restUrl, Object input, Map<String, String> headers, String httpMethod, String inputType) {
		if ("POST".equalsIgnoreCase(httpMethod)) {
			return (Map<String, ?>) httpPost(URL, input, headers, inputType);
		} else if ("GET".equalsIgnoreCase(httpMethod)) {
			return (Map<String, ?>) httpGet(URL, headers);
		} else if ("PUT".equalsIgnoreCase(httpMethod)) {
			return (Map<String, ?>) httpPut(URL, input, headers, inputType);
		} else if ("DELETE".equalsIgnoreCase(httpMethod)) {
			return (Map<String, ?>) httpDelete(URL, headers);
		} else {
			logger.info("Error with data input.");
			return false;
		}
	}*/

	static httpGet(String restUrl, Map<String, String> headerMap) {
		def client = new RESTClient(restUrl)
		logger.info "Executing a GET on " + restUrl
		double start = System.currentTimeMillis()
		try{
			def response = client.get(headers: headerMap)
			double responseTime = ((System.currentTimeMillis() - start)/1000).round(3)
			return ["response":response, "responseTime":responseTime]
		} catch (RESTClientException e) {
			double responseTime = ((System.currentTimeMillis() - start)/1000).round(3)
			if (e.response) {
				return ["response": e.response, "responseTime":responseTime]
			} else {
				return ["response": e, "responseTime":responseTime]
			}
		}
	}

	/**
	 * Method to call Rest service using PUT method.
	 * 
	 * @param restUrl
	 * @param input
	 * @param headerMap
	 * @return
	 */
	static httpPut(String restUrl, Object input, Map<String, String> headerMap, String inputType) {
		def client = new RESTClient(restUrl)
		logger.info "Executing a PUT on " + restUrl
		double start = System.currentTimeMillis()
		try {
			def response;
			if ("text".equalsIgnoreCase(inputType)) {
				response = client.put(headers: headerMap) {text input }
			} else {
				response = client.put(headers: headerMap) {json input }
			}
			double responseTime = ((System.currentTimeMillis() - start)/1000).round(3)
			return ["response":response, "responseTime":responseTime]
		} catch (RESTClientException e) {
			double responseTime = ((System.currentTimeMillis() - start)/1000).round(3)
			if (e.response) {
				return ["response":e.response, "responseTime":responseTime]
			} else {
				return ["response":e, "responseTime":responseTime]
			}
		}
	}
	
	/**
	 * Method to call Rest service using POST method.
	 * 
	 * @param restUrl
	 * @param inputJSON
	 * @param headerMap
	 * @return
	 */
	static httpPost(String restUrl, Object input, Map<String, String> headerMap, String inputType) {
		def client = new RESTClient(restUrl)
		logger.info "Executing a POST on " + restUrl
		
		double start = System.currentTimeMillis()
		try{
			def response;
			if ("text".equalsIgnoreCase(inputType)) {
				response = client.post(headers: headerMap) {text input }
			} else {
				response = client.post(headers: headerMap) {json input }
			}
			double responseTime = ((System.currentTimeMillis() - start)/1000).round(3)
			return ["response":response, "responseTime":responseTime]
		} catch (RESTClientException e) {
			double responseTime = ((System.currentTimeMillis() - start)/1000).round(3)
			if (e.response) {
				return ["response":e.response, "responseTime":responseTime]
			} else {
			return ["response":e, "responseTime":responseTime]
			}
		}
	}
	
	/**
	 * Method to call Rest service using DELETE method.
	 * 
	 * @param restUrl
	 * @param inputJSON
	 * @param headerMap
	 * @return
	 */
	static httpDelete(String restUrl, Map<String, String> headerMap) {
		def client = new RESTClient(restUrl)
		logger.info "Executing a DELETE on " + restUrl
		double start = System.currentTimeMillis()
		try{
			def response = client.delete(headers: headerMap)
			double responseTime = ((System.currentTimeMillis() - start)/1000).round(3)
			return ["response":response, "responseTime":responseTime]
		} catch (RESTClientException e) {
			double responseTime = ((System.currentTimeMillis() - start)/1000).round(3)
			if (e.response) {
				return ["response":e.response, "responseTime":responseTime]
			} else {
				return ["response":e, "responseTime":responseTime]
			}
		}
	}
}