package com.moto.common.ws.automation.driver;

import java.util.Map;

import org.json.simple.JSONArray;

public class TestScenarioInfo {
	private String uniqueId;
	private String description;
	private String httpMethod;
	private String expectedStatusCode;
	private Map<String, String> headers;
	private String URL;
	private String inputType;
	private Object inputJSON;
	private Object expectedJSON;
	private JSONArray includeFields;
	private JSONArray excludeFields;
	private String endPoint;
	private String validatorClassName;
	private JSONArray validatorParameters;

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getExpectedStatusCode() {
		return expectedStatusCode;
	}

	public void setExpectedStatusCode(String expectedStatusCode) {
		this.expectedStatusCode = expectedStatusCode;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public Object getInputJSON() {
		return inputJSON;
	}

	public void setInputJSON(Object inputJSON) {
		this.inputJSON = inputJSON;
	}

	public Object getExpectedJSON() {
		return expectedJSON;
	}

	public void setExpectedJSON(Object expectedJSON) {
		this.expectedJSON = expectedJSON;
	}

	public JSONArray getIncludeFields() {
		return includeFields;
	}

	public void setIncludeFields(JSONArray includeFields) {
		this.includeFields = includeFields;
	}

	public JSONArray getExcludeFields() {
		return excludeFields;
	}

	public void setExcludeFields(JSONArray excludeFields) {
		this.excludeFields = excludeFields;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getValidatorClassName() {
		return validatorClassName;
	}
	
	public void setValidatorClassName(String validatorClassName) {
		this.validatorClassName = validatorClassName;
	}

	public JSONArray getValidatorParameters() {
		return validatorParameters;
	}
	

	public void setValidatorParameters(JSONArray validatorParameters) {
		this.validatorParameters = validatorParameters;
	}
}