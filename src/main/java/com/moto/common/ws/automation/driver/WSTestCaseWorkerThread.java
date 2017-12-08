package com.moto.common.ws.automation.driver;

import java.io.File;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import wslite.http.HTTPResponse;
import wslite.rest.Response;

import com.jayway.jsonpath.JsonPath;
import com.moto.common.util.CommonUtility;
import com.moto.common.util.validator.Validator;
import com.moto.common.ws.automation.client.RestClientUtility;
import com.moto.common.ws.automation.utility.CompareUtility;
import com.moto.common.ws.automation.utility.PublicUtility;
import com.moto.common.ws.automation.utility.WebserviceUtility;

public class WSTestCaseWorkerThread implements Callable<Map<String, String>> {
	private static final Logger logger = LogManager.getLogger(WSTestCaseWorkerThread.class);
	private static final FastDateFormat dateFormat = FastDateFormat.getInstance("MM/dd/yyyy HH:mm:ss", TimeZone.getTimeZone("GMT"));
	
	private boolean isHealthCheck;
	private JSONObject jsonConfig;
	//private JSONObject serviceObj;
	private String testCaseScenario;
	private String baseURL;
	private Map<String, String> auxURLs;
	private Date executionStartDate;
	private Integer suitId;
	private String env;
	private Map<String, Object> serviceResponseMap = new HashMap<String, Object>();
	private final Date currentDate = new Date();
	//private String logData = "";
	// Map containing the failed tests
	private Map<String, String> failedTestsMap;
	private HashMap<Integer,String> suiIdgroupMap;
	private static boolean setResultFlag = true;
	public WSTestCaseWorkerThread(JSONObject jsonConfig, JSONObject serviceObj, String testCaseScenario, Date executionStartDate, Integer suitId, String env,HashMap<Integer,String> suiIdgroupMap) {
		this.jsonConfig = jsonConfig;
		//this.serviceObj = serviceObj;
		this.testCaseScenario = testCaseScenario;
		this.executionStartDate = executionStartDate;
		this.suitId = suitId;
		this.env = env;
		this.suiIdgroupMap = suiIdgroupMap;
		this.isHealthCheck = BooleanUtils.toBoolean(System.getProperty("health-check"));
		/*if (StringUtils.isEmpty(env)) {
			//this.baseURL = System.getProperty("base-url"); //System.getProperty("base-url", (String) jsonConfig.get("baseURL"));
			this.baseURL = System.getProperty("base-url", (String) jsonConfig.get("baseURL"));
		} else {
			this.baseURL = (String) jsonConfig.get(env + "-URL");
		}*/
		
		if (isHealthCheck) {
			this.baseURL = (String) jsonConfig.get(env + "-URL");
		} else {
			this.baseURL = System.getProperty("base-url", (String) jsonConfig.get("baseURL"));
		}

		loadAuxURLs(jsonConfig);  
		this.failedTestsMap = new HashMap<String, String>();
	}

	private void loadAuxURLs(final JSONObject jsonConfig) {
		auxURLs = new HashMap<String, String>();
		final JSONArray auxList = (JSONArray) jsonConfig.get("aux_URLs");
		if (auxList != null) {
			for (final Object aux : auxList) {
				final JSONObject json = (JSONObject) aux;
				for (final Object obj : json.keySet()) {
					final String key = (String) obj;
					if (!auxURLs.containsKey(key)) {
						final String urlParam = System.getProperty(key);
						auxURLs.put(key, StringUtils.isNotBlank(urlParam) ? urlParam : (String) json.get(key));
					}
				}
			}
		} else if (StringUtils.isNotBlank(env)) {
			auxURLs = MasterData.getUrls();
		}
	}

	@Override
	public Map<String, String> call() throws Exception {
		this.run();
		return failedTestsMap;
	}
	
	public void run() {
		ThreadContext.put("ROUTINGKEY", WSTestCaseWorkerThread.class.getName());
		logger.info(Thread.currentThread().getName() + " Start. Command = " + testCaseScenario);
		process();
		logger.info(Thread.currentThread().getName() + " End.");
		ThreadContext.remove(WSTestCaseWorkerThread.class.getName());
	}
	
	@SuppressWarnings("unchecked")
	private void process() {
		try {
			JSONObject json = null;
			final String source = (String) jsonConfig.get("source");
			Long testId = null;
			
			if ("folder".equals(source)) {
				json = PublicUtility.retrieveJSONBasedOnFileName(testCaseScenario);
			} else if ("db".equals(source)) {
				String strTestDBJson = null;
				final JSONObject testDBJson = (JSONObject) jsonConfig.get("testDB");
				synchronized (testDBJson) {
					final String scenario = (System.getProperty("scenarioName") != null && !(System.getProperty("scenarioName").isEmpty()))?System.getProperty("scenarioName"):(String) testDBJson.get("scenarioName");
					final String testLocale = (System.getProperty("locale") != null && !(System.getProperty("locale").isEmpty()))?System.getProperty("locale"):(String) testDBJson.get("locale");
					
					testDBJson.put("locale", testLocale);
					testDBJson.put("scenarioName", scenario);
					testDBJson.put("testScenarioName", testCaseScenario);
					testDBJson.put("groupName", suiIdgroupMap.get(suitId));
					strTestDBJson = testDBJson.toString();
				}
				
				final JSONObject testDataJson = PublicUtility.retrieveTestDataFromDB(strTestDBJson);
				
				
				testId = JsonPath.read(testDataJson, "$.testId");
				final String testData = (String) testDataJson.get("testData");
				json = (JSONObject) PublicUtility.retrieveJSONFromString(testData.trim());
			} else {
				throw new Exception("Invalid test scenario name");
			}

			if (json == null) {
				throw new Exception("Invalid test scenario name");
			}
			final JSONArray testArray = prepareJSONData((JSONArray) json.get("restCalls"));
			executeTest(testId, testArray, testCaseScenario);
		} catch (Exception e) {
			logger.error("Error in JSON for " + testCaseScenario + ". Skipping this test. " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private JSONArray prepareJSONData(final JSONArray testArr) {
		return this.prepareJSONData(testArr, "@");
	}
	
	private JSONArray prepareJSONData(final JSONArray testArr, final String separator) {
		if (testArr == null) {
			return null;
		}
		
		String str = testArr.toJSONString();
		final String[] subStrArr = StringUtils.substringsBetween(str, separator, separator);
		if (ArrayUtils.isEmpty(subStrArr)) {
			return testArr;
		}
		
		for (final String subStr : subStrArr) {
			final String strArr[] = StringUtils.split(subStr, ":");
			if (ArrayUtils.isNotEmpty(strArr)) {
				if ("ecommOrderId".equalsIgnoreCase(strArr[0])) {
					str = str.replaceAll(separator + subStr + separator, "AutoTest_" + currentDate.getTime());
				} else if ("getCurrentDate".equalsIgnoreCase(strArr[0])) {
					String pattern = strArr[1];
					for (int i = 2; i < strArr.length; ++i) {
						pattern += ":" + strArr[i];
					}
					final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
					str = str.replaceAll(separator + subStr + separator, sdf.format(currentDate));
				}
			}
		}
		JSONArray json = null;
		try {
			json = (JSONArray) new JSONParser().parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	private void executeTest(final Long testId, final JSONArray testArray, final String testScenarioName) {
		boolean isTestCasePassed = false;
		String logData = "";
		final int numTests = testArray.size();
		TimeZone.setDefault(TimeZone.getTimeZone("CST"));
		Date startTime = new Date();
		
		int tokenRandomNumber = 0 ;
		for (int i = 0; i < numTests; i++) {
			final JSONObject restCall = (JSONObject) testArray.get(i);

			try {
				final String waitTime = ConvertUtils.convert(restCall.get("waitTimeInSecond"));
				if (NumberUtils.isNumber(waitTime)) {
					Thread.sleep(NumberUtils.toLong(waitTime) * 1000);
				} else if ("${waitTimeInSecond}".equalsIgnoreCase(waitTime)) {
					Thread.sleep(MasterData.getWaitTimeInSecond() * 1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			final TestScenarioInfo testScenarioInfo = new TestScenarioInfo();
			testScenarioInfo.setUniqueId((String) restCall.get("uniqueId"));
			testScenarioInfo.setDescription((String) restCall.get("description"));
			testScenarioInfo.setHttpMethod((String) restCall.get("httpMethod"));
			testScenarioInfo.setEndPoint((String) restCall.get("endPoint"));
			testScenarioInfo.setExpectedStatusCode((String) restCall.get("expectedStatusCode"));
			testScenarioInfo.setHeaders(processHeaders((JSONObject)restCall.get("headers")));
			
			String URL = (String) restCall.get("url");
			if (URL.contains("${baseURL}")) {
				URL = URL.replace("${baseURL}", baseURL);
			} else {
				try {
					final String auxURL = URL.substring(URL.indexOf("$"), URL.indexOf("}") + 1);
					URL = URL.replace(auxURL, auxURLs.get(auxURL.substring(2, auxURL.length() - 1)));
				} catch (Exception e) {
					logger.error("Test case number: " + testId + ", Scenario Name : " + testScenarioName);
					e.printStackTrace();
				}
			}
			URL = replaceTokenWithValue(URL);
            testScenarioInfo.setURL(URL);

            testScenarioInfo.setInputType((String)restCall.get("inputType"));
            if ("text".equalsIgnoreCase(testScenarioInfo.getInputType())) {
            	testScenarioInfo.setInputJSON(this.replaceTokenWithValue(restCall.get("inputJSON").toString()));
            } else {
            	testScenarioInfo.setInputJSON(PublicUtility.retrieveJSONFromString(this.replaceTokenWithValue(restCall.get("inputJSON").toString())));
            }
            if("BLC_API_AUTO".equals(System.getProperty("group").toUpperCase())){
            	if(testScenarioInfo.getURL().contains("m4ds") && testScenarioInfo.getURL().contains("/design")){
            		JSONObject inputJson = (JSONObject)testScenarioInfo.getInputJSON();
            		String inputLocalToken = (String)inputJson.get("localToken");
            		String[] tokens = inputLocalToken.split("_");
            		
            		final JSONObject jsonM4DesignPayload = PublicUtility.retrieveJSONBasedOnFileName(jsonConfig.get("payloadFilePath") + File.separator + tokens[2]+"_payloadM4Design.json");
        			inputJson.put("payload", jsonM4DesignPayload.get(tokens[1]));
            		
            		tokenRandomNumber = CommonUtility.generateNum();
            		inputLocalToken += "_"+tokenRandomNumber;
            		inputJson.put("localToken", inputLocalToken);
            		testScenarioInfo.setInputJSON(inputJson);
            		
            	}else if(testScenarioInfo.getURL().contains("/motomaker?token=")){
            		if(tokenRandomNumber != 0){
	            		String inputURL = (String)testScenarioInfo.getURL();
	            		inputURL += "_"+tokenRandomNumber;
	            		testScenarioInfo.setURL(inputURL);
            		}
            	}
            }
            
        	testScenarioInfo.setExpectedJSON(PublicUtility.retrieveJSONFromString(this.replaceTokenWithValue(restCall.get("expectedJSON").toString())));

			testScenarioInfo.setIncludeFields((JSONArray) restCall.get("includeFields"));
			testScenarioInfo.setExcludeFields((JSONArray) restCall.get("excludeFields"));
			
			testScenarioInfo.setValidatorClassName((String) restCall.get("validatorClassName"));
			testScenarioInfo.setValidatorParameters((JSONArray) restCall.get("validatorParameters"));
			
			final String testNumber = testScenarioName + "; Call: " + (i+1);
			isTestCasePassed = runWebservice(testScenarioInfo, testNumber);
			//Capture all the responses of a scenario and assign to logData to display in Result
			if(!(("DCG".equals(System.getProperty("group"))) || ("LESC".equals(System.getProperty("group"))) || ("envdata".equals(System.getProperty("group"))))){
				if("BLC_API_AUTO".equals(System.getProperty("group").toUpperCase())){
					if(restCall.get("uniqueId")!= null && restCall.get("uniqueId").toString().contains("perform") && isTestCasePassed){
						JSONObject OrderNumberextr = (JSONObject)serviceResponseMap.get((String) restCall.get("uniqueId"));
						String OrderNum = OrderNumberextr.get("blcOrderNumber").toString();
						logData += (String) restCall.get("endPoint")+" :: Response Code - "+testScenarioInfo.getExpectedStatusCode()+"\n";
						logData += OrderNum;
					}else{
						logData += (String) restCall.get("endPoint")+" :: Response Code - "+testScenarioInfo.getExpectedStatusCode()+"\n";
					}
				}else{ //Nishant - Added log line for OPS regression scripts
					logData += (String) restCall.get("endPoint")+" :: Response Code - "+testScenarioInfo.getExpectedStatusCode()+"\n";
				}
				
				if(!isTestCasePassed){
					logData += "Response Message - "+serviceResponseMap.get((String) restCall.get("uniqueId"))+"\n\n";
				}
			}
			if("DCG".equals(System.getProperty("group")) || "LESC".equals(System.getProperty("group")) || "envdata".equals(System.getProperty("group"))){
				
				if(isTestCasePassed){ 
				JSONObject responseJSon = (JSONObject)serviceResponseMap.get((String) restCall.get("uniqueId"));
				JSONArray tabsArray = (JSONArray)responseJSon.get("tabs");
				for(Object eachtab : tabsArray){
					JSONObject eachtabJson  = (JSONObject)eachtab;
					String Title = eachtabJson.get("title").toString();
					JSONArray subTabArray  = (JSONArray)eachtabJson.get("subTabs");
					for(Object eachSubTab : subTabArray){
						JSONObject eachsubtabJson  = (JSONObject)eachSubTab;
						String SubTitle = eachsubtabJson.get("title").toString();
						JSONArray sectionArray = (JSONArray)eachsubtabJson.get("sections");
						for(Object eachSectionArray : sectionArray){
							JSONObject eachSectionArrayJson  = (JSONObject)eachSectionArray;
							String AdditionalSubTitle = eachSectionArrayJson.get("title").toString();
							if(eachSectionArrayJson.containsKey("errors")){
								JSONArray ErrorArray  = (JSONArray)eachSectionArrayJson.get("errors");
								if(!ErrorArray.isEmpty()){ 
									for(Object eachError : ErrorArray){	 
										JSONObject eachErrorJson = (JSONObject)eachError;
										String ErrorMsg = eachErrorJson.get("message").toString();
										logData +=  (String) restCall.get("uniqueId")+"-"+Title +"-"+SubTitle+"-"+AdditionalSubTitle+"\n"+ErrorMsg+"\n";
										isTestCasePassed = false;
									}	
								} 
						}
						
					}
					
				}
				}
				if(isTestCasePassed)
					logData+=(String) restCall.get("uniqueId")+"- Works as expected, No errors found";
			}else{
				logData += (String) restCall.get("uniqueId") +"-" +serviceResponseMap.get((String) restCall.get("uniqueId"))+"\n\n";
			}
			
			List<String> testGroupList = new ArrayList<String>(Arrays.asList("BLC_API_AUTO", "BRUCE_API_AUTO", "OPS-SMOKE", "REPORTS", "MICRO", "DCG", "LESC","envdata","PAYUXE","OPS"));
			
			if(!isTestCasePassed && (testGroupList.contains(System.getProperty("group").toUpperCase()))){
				//if(!isTestCasePassed && ("BLC_API_AUTO".equals(System.getProperty("group").toUpperCase()) || "BRUCE_API_AUTO".equals(System.getProperty("group").toUpperCase()) || "OPS-SMOKE".equals(System.getProperty("group").toUpperCase()))){
				break;
			}
			
			/*if (numTests > 1 && !isTestCasePassed) {
				logger.error("Test case number: " + i+1 + " couldn't pass, further test case(s) will not be executed. Scenario Name : " + testScenarioName);
				break;
			}*/
			}
		}
		
		if ("db".equals((String) jsonConfig.get("source")) && suitId != null && suitId > 0) {
			if(!System.getProperty("group").toUpperCase().equals("REPORTS") || setResultFlag){
				final Map<String, Object> resultTestCaseMap = new HashMap<>();
				resultTestCaseMap.put("testId", testId);
				resultTestCaseMap.put("status", (isTestCasePassed ? "Pass" : "Fail"));
				
				//resultTestCaseMap.put("log", ThreadContext.get(WSTestCaseWorkerThread.class.getName()));
				resultTestCaseMap.put("log", logData);
				//resultTestCaseMap.put("image", "");
				//resultTestCaseMap.put("video", "");
				resultTestCaseMap.put("startDtTime", dateFormat.format(startTime));
				resultTestCaseMap.put("endDtTime", dateFormat.format(new Date()));
				resultTestCaseMap.put("updtBy", "itauto1");
				
				final Set<JSONObject> resultTestCaseMapSet = new HashSet<>(1);
				resultTestCaseMapSet.add(new JSONObject(resultTestCaseMap));
				
				final Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("suitId", suitId);
				resultMap.put("resultTestCases", resultTestCaseMapSet);
				
				final JSONObject resultJson = new JSONObject(resultMap);
				
				final String result = new PublicUtility().setTestExecutResult(resultJson.toJSONString());
				//final String result = PublicUtility.setTestExecutionResult(resultJson.toJSONString());
				logger.info(result);
				if(System.getProperty("group").toUpperCase().equals("REPORTS")){
					setResultFlag = false;
				}
			}
		}
	}
	
	private String replaceTokenWithValue(String str) {
		return this.replaceTokenWithValue(str, "@");
	}

	/**
	 * Method responsible for replacing a string with it's corresponding value in the serviceResponseMap in case it is a chained call dependent on a result from another call
	 * 
	 * @param str the return value replaced by either the full Json, an attribute within this Json or the parameter itself
	 * @param separator string to recover identifier to replace
	 * @param isFullJson flag indicating whether the value to replace is the full Json object (to put in the request body) or an attribute within the Json object (to put in the request URL)
	 * @return
	 */
	private String replaceTokenWithValue(String str, final String separator) {
		final String[] subStrArr = StringUtils.substringsBetween(str, separator, separator);
		if (ArrayUtils.isEmpty(subStrArr)) {
			return str;
		}
		
		for (final String subStr : subStrArr) {
			final String strArr[] = StringUtils.split(subStr, ":");
			if (ArrayUtils.isNotEmpty(strArr) && strArr.length == 2) {
				final Object jsonObj = serviceResponseMap.get(strArr[0]);
				if (jsonObj != null) {
					try {
						final String strVal = JsonPath.read(jsonObj, strArr[1]).toString();
						str = str.replace(separator + subStr + separator, strVal);
					} catch(Exception e) {}
				}
			}
		}
		
		return str;
	}
	
	private Map<String, String> processHeaders(JSONObject headersData) {
		final Map<String, String> headers = new HashMap<String, String>();
		
		if (headersData != null) {
			for (final Object currentKeyObj : headersData.keySet()) {
				final String currentKey = (String) currentKeyObj;
				String currentValue = (String) headersData.get(currentKey);
				if (MasterData.getJsonMasterConfig() != null && 
						StringUtils.startsWith(currentValue, "${") && StringUtils.endsWith(currentValue, "}")) {
					currentValue = StringUtils.removeStart(StringUtils.removeEnd(currentValue, "}"), "${");
					currentValue = JsonPath.read(MasterData.getJsonMasterConfig(), "$."+currentKey.toLowerCase()+"."+currentValue);
				}
				headers.put(currentKey, currentValue);
			}
		}
		return headers;
	}

	@SuppressWarnings("unchecked")
	private boolean runWebservice(final TestScenarioInfo testScenarioInfo, final String testNumber) {
		//final String testNumber = testScenarioName + "; Call: " + testNum;
		
		final String httpMethod = testScenarioInfo.getHttpMethod();
		final String URL = testScenarioInfo.getURL();
		final Object inputJSON = testScenarioInfo.getInputJSON();
		final Map<String, String> headers = testScenarioInfo.getHeaders();
		final Object expectedJSON = testScenarioInfo.getExpectedJSON();
		final String expectedStatusCode = testScenarioInfo.getExpectedStatusCode();
		final JSONArray includeFields = testScenarioInfo.getIncludeFields();
		final JSONArray excludeFields = testScenarioInfo.getExcludeFields();
		final String inputType = testScenarioInfo.getInputType();
		
		RestClientUtility.httpsSetup(URL);
		
		Map<String, ?> responseMap = null;
		if ("POST".equalsIgnoreCase(httpMethod)) {
			responseMap = (Map<String, ?>) WebserviceUtility.httpPost(URL, inputJSON, headers, inputType);
		} else if ("GET".equalsIgnoreCase(httpMethod)) {
			responseMap = (Map<String, ?>) WebserviceUtility.httpGet(URL, headers);
		} else if ("PUT".equalsIgnoreCase(httpMethod)) {
			responseMap = (Map<String, ?>) WebserviceUtility.httpPut(URL, inputJSON, headers, inputType);
		} else if ("DELETE".equalsIgnoreCase(httpMethod)) {
			responseMap = (Map<String, ?>) WebserviceUtility.httpDelete(URL, headers);
		} else {
			logger.info("Error with data input.");
			return false;
		}
		
		String responseData = null;
		if (responseMap.get("response") instanceof Response) {
			responseData = ((Response) responseMap.get("response")).getResponse().getContentAsString();
		} else if (responseMap.get("response") instanceof HTTPResponse) {
			responseData = ((HTTPResponse) responseMap.get("response")).getContentAsString();
		}

		// validate response
		//boolean isTestCasePassed = CompareUtility.compareJson(responseMap, testNumber, expectedJSON, expectedStatusCode, URL, httpMethod, includeFields, excludeFields);
		boolean isTestCasePassed = responseData == null ? false : true;
		if (!StringUtils.containsIgnoreCase(URL, "https://checkout-sandbox.gointerpay.net")) {
			isTestCasePassed = CompareUtility.compareJson(responseMap, testNumber, expectedJSON, expectedStatusCode, URL, httpMethod, includeFields, excludeFields);
		}

		// validate custom function defined by the implementation of the Validator interface, if there is any provided
		if (isTestCasePassed && StringUtils.isNotBlank(testScenarioInfo.getValidatorClassName())) {
			 isTestCasePassed = validateCustomFunction(testScenarioInfo, responseData);
			// process failure
			if (!isTestCasePassed) {
				// save failed test for report
				OutputData.processErrorFunctionValidation(testNumber, expectedStatusCode, responseMap.get("response"), testScenarioInfo.getValidatorClassName(), (Double) responseMap.get("responseTime"), URL, httpMethod);
				// save failed test build error
				this.failedTestsMap.put(testScenarioInfo.getUniqueId(), testScenarioInfo.getValidatorParameters().toJSONString());
			}
		}

		if (StringUtils.isNotEmpty(testScenarioInfo.getUniqueId()) && StringUtils.isNotEmpty(responseData)) {
			Object json = null;
			
			if (StringUtils.containsIgnoreCase(URL, "https://checkout-sandbox.gointerpay.net")) {
				try {
					responseData = "{" + StringUtils.substringBetween(URLDecoder.decode(responseData, "UTF-8"), "{", "}") + "}";
					json = PublicUtility.retrieveJSONFromString(responseData);
				} catch (Exception e) {
					e.printStackTrace();
					// it is not possible to parse at all
					logger.error(String.format("Problems trying to parse the response (https://checkout-sandbox.gointerpay.net) from the following request %s. Response: %s", new Object[]{testScenarioInfo.getUniqueId(), responseData}), e);
				}
			} else {
				try {
					json = PublicUtility.retrieveJSONFromString(responseData);
				} catch (Exception ex) {
					logger.error(String.format("Problems trying to parse the response from the following request %s. Response: %s", new Object[]{testScenarioInfo.getUniqueId(), responseData}), ex);
				}
			}
			if (json != null) {
				serviceResponseMap.put(testScenarioInfo.getUniqueId(), json);
			}
		}
		
		// if the test is successful, save data for report
		if (isTestCasePassed) {
			OutputData.processPass(testNumber, expectedStatusCode, (Double) responseMap.get("responseTime"), URL, httpMethod);
		}
		
		
		if (isHealthCheck && executionStartDate != null) {
			final JSONObject healthCheckMaster =  new JSONObject();
			if (env == null) {
				env = System.getProperty("env");
			}
			healthCheckMaster.put("env", env != null ? env.toUpperCase() : null);
			healthCheckMaster.put("group", System.getProperty("group").toUpperCase());
			healthCheckMaster.put("locale", System.getProperty("locale").toUpperCase());
			healthCheckMaster.put("serviceMethod", httpMethod);
			healthCheckMaster.put("envBaseURL", getURL(testScenarioInfo));
			healthCheckMaster.put("serviceEndPoint", testScenarioInfo.getEndPoint());
			
			final JSONObject healthCheckResult =  new JSONObject();
			healthCheckResult.put("serviceHealthCheckMasterDTO", healthCheckMaster);
			healthCheckResult.put("executionTime", responseMap.get("responseTime"));
			healthCheckResult.put("passed", isTestCasePassed);
			healthCheckResult.put("startedAt", dateFormat.format(executionStartDate));
			
			if (!isTestCasePassed && !this.failedTestsMap.containsKey(healthCheckResult.toJSONString())) {
				this.failedTestsMap.put(healthCheckResult.toJSONString(), healthCheckResult.toJSONString());
			}
			
			if (isHealthCheck) {
				final String result = PublicUtility.setHealthCheckResult(healthCheckResult.toJSONString());
				logger.info("Health Check Result : " + result);
			}
		}
		return isTestCasePassed;
	}

	/**
	 * Validate a custom function defined by the implementation of the {@link Validator} class.
	 * 
	 * @param info Test Scenario information.
	 * 
	 * @return <code>true</code> if the validation is successful, <code>false</code> otherwise.
	 */
	private boolean validateCustomFunction(final TestScenarioInfo info, final String jsonData) {
		boolean isTestCasePassed = false;
		try {
			// get validator parameters, if there is any
			String[] validatorParameters = null;
			final JSONArray validatorParametersJSON = info.getValidatorParameters();
			if (validatorParametersJSON != null && validatorParametersJSON.size() > 0) {
				validatorParameters = new String[validatorParametersJSON.size()];
				for (int index = 0; index < validatorParametersJSON.size(); index++) {
					validatorParameters[index] = (String) validatorParametersJSON.get(index);
					// replace variable with value
					validatorParameters[index] = this.replaceTokenWithValue(validatorParameters[index]);
				}
			} else {
				validatorParameters = new String[]{};
			}
			 
			// get the validator and execute it
			final Class<?> validatorClass = Class.forName(info.getValidatorClassName());
			final Validator validatorInstance = (Validator) validatorClass.newInstance();
			final String error[] = validatorInstance.validate(validatorParameters, jsonData);
			if (ArrayUtils.isEmpty(error)) {
				isTestCasePassed = true;
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			logger.error("Problems while executing custom validator.", ex);
			isTestCasePassed = false;
		}
		return isTestCasePassed;
	}

	private String getURL(final TestScenarioInfo testScenarioInfo) {
		if (testScenarioInfo.getURL().contains(baseURL)) {
			return baseURL;
		} else {
			for(final String auxURL : auxURLs.values()) {
				if (testScenarioInfo.getURL().contains(auxURL)) {
					return auxURL;
				}
			}
		}
		return testScenarioInfo.getURL();
	}
}
