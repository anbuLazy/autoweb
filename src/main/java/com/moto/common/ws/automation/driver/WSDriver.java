package com.moto.common.ws.automation.driver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.jayway.jsonpath.JsonPath;
import com.moto.common.util.CommonUtility;
import com.moto.common.util.mail.MailInfo;
import com.moto.common.util.mail.MailUtil;
import com.moto.common.ws.automation.utility.PublicUtility;
import com.moto.common.ws.automation.utility.ReportUtility;;

public class WSDriver {
	private static final Logger logger = LogManager.getLogger(WSDriver.class);
	
	private static final FastDateFormat dateFormat = FastDateFormat.getInstance("MM/dd/yyyy HH:mm:ss", TimeZone.getTimeZone("GMT"));
	
	private final boolean isHealthCheck = BooleanUtils.toBoolean(System.getProperty("health-check"));
	private String masterConfigFile = null;
	private String configFile = null;
	private String env = null;
	private Date startDate = null;
	// Map containing the failed tests
	private Map<String, String> failedTestsMap = new HashMap<String, String>();
	
	public WSDriver(final String configFile, final String env, final Date startDate) {
		this.configFile = configFile;
		this.env = env;
		this.startDate = startDate;
	}
	
	public WSDriver(final String masterConfigFile, final String configFile, final String env, final Date startDate) {
		this.masterConfigFile = masterConfigFile;
		this.configFile = configFile;
		this.env = env;
		this.startDate = startDate;
	}
	
	public Map<String, String> getFailedTestsMap() {
		return this.failedTestsMap;
	}

	@SuppressWarnings("unchecked")
	public void execute() {
		final JSONObject jsonMasterConfig = PublicUtility.retrieveJSONBasedOnFileName(masterConfigFile);
		MasterData.setJsonMasterConfig(jsonMasterConfig);
		//ANBU
		HashMap<Integer,Integer> suitcountMap = new HashMap<Integer,Integer>();
		HashMap<Integer,JSONObject> suitJsonMap = new HashMap<Integer,JSONObject>();
		HashMap<Integer,Integer> suittestscenariocountMap = new HashMap<Integer,Integer>();
		HashMap<Integer,Map<String, List<String>>> suittestMap = new HashMap<Integer,Map<String, List<String>>>();
		HashMap<Integer, String> suiIdgroupMap = new HashMap<Integer, String>();
		
		
		final JSONObject jsonConfig = PublicUtility.retrieveJSONBasedOnFileName(configFile);
		assertNotNull("Invalid config file.", jsonConfig);
		MasterData.setJsonConfig(jsonConfig);
		
		if (StringUtils.isNotBlank(env) && jsonMasterConfig != null) {
			MasterData.setUrls(new HashMap<String, String>((JSONObject) JsonPath.read(jsonMasterConfig, "$.envURL." + env)));
			
			try {
				final Long waitTimeInSecond = (Long) JsonPath.read(jsonMasterConfig, "$.customProp." + env + ".waitTimeInSecond");
				MasterData.setWaitTimeInSecond(waitTimeInSecond);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		int threadCount = NumberUtils.toInt((String) jsonConfig.get("serviceThreadCount"));
		long serviceThreadTimeout = NumberUtils.toLong((String) jsonConfig.get("serviceThreadTimeout"));
		
		if (threadCount < 1) {
			logger.error("serviceThreadCount count is not greater than 0 or non numeric in ops_config.");
			return;
		}

		Map<String, List<String>> serviceTestCaseMap = null;
		if ("folder".equals((String) jsonConfig.get("source"))) {
			serviceTestCaseMap = getServiceTestCaseMapping(jsonConfig);
		} else if ("db".equals((String) jsonConfig.get("source"))) {
			serviceTestCaseMap = getServiceTestCaseMappingFromDB(jsonConfig);
		} else {
			fail("No source is defined in config.");
			return;
		}int testScenarioCount = 0;
		if(!"LESC  Automation".equals((String) jsonConfig.get("description"))){
			assertNotNull("serviceTestCaseMap should not be null.", serviceTestCaseMap);
			assertTrue("serviceTestCaseMap should not be empty.", serviceTestCaseMap.size() > 0);
		}
		logger.info(serviceTestCaseMap);
			for (final List<String> testScenario : serviceTestCaseMap.values()) {
				testScenarioCount += testScenario.size();
		}
		if(!"LESC  Automation".equals((String) jsonConfig.get("description"))){
		assertTrue("testScenarioCount should not be 0.", testScenarioCount > 0);
		}
		threadCount = threadCount <= serviceTestCaseMap.size() ? threadCount : serviceTestCaseMap.size();
		  JSONArray serviceArray = (JSONArray) jsonConfig.get("testRunOn");
		int testCount = serviceArray.size();
		  int numTests = testCount;
		
		final double start = System.currentTimeMillis();
		JSONObject  CtoLocaleJson = null ;
		JSONArray CtoJSON = null;
		
		Integer suitId = null;
		List<Integer> suitIdArray = new ArrayList<Integer>();
		if ("db".equals((String) jsonConfig.get("source"))) {
			final Map<String, Object> resultMap = new HashMap<String, Object>();
			
			final JSONObject testDBJson = (JSONObject) jsonConfig.get("testDB");
			
			if("BLC_UAT".equals((String) testDBJson.get("env")) || "BLC_API".equals((String) testDBJson.get("env"))){
				
				resultMap.put("env", (String)testDBJson.get("env"));
				resultMap.put("groupName", (String)testDBJson.get("groupName"));
				String testLocale = (System.getProperty("locale") != null && !(System.getProperty("locale").isEmpty()))?System.getProperty("locale"):(String) testDBJson.get("locale");
				resultMap.put("locale", testLocale);
				
			}else if("BRUCE_API".equals((String) testDBJson.get("env"))){
				resultMap.put("env", (String)testDBJson.get("env"));
				resultMap.put("groupName", (String)testDBJson.get("groupName"));
				String testLocale = (System.getProperty("locale") != null && !(System.getProperty("locale").isEmpty()))?System.getProperty("locale"):(String) testDBJson.get("locale");
				resultMap.put("locale", testLocale);
				
			}else if("OPS_SMOKE".equals((String) testDBJson.get("env"))){
				resultMap.put("env", (String)testDBJson.get("env"));
				resultMap.put("groupName", (String)testDBJson.get("groupName"));
				String testLocale = (System.getProperty("locale") != null && !(System.getProperty("locale").isEmpty()))?System.getProperty("locale"):(String) testDBJson.get("locale");
				resultMap.put("locale", testLocale);
				
			}else if("DCG".equals((String) testDBJson.get("env")) ||"LESC".equals((String) testDBJson.get("env"))||"envdata".equals((String) testDBJson.get("env"))){
				resultMap.put("env", (String)testDBJson.get("env"));
				String groupNam = (String)testDBJson.get("groupName");
				String Locale = System.getProperty("locale").toString();
				if(groupNam.equalsIgnoreCase("ALL")){
					  CtoLocaleJson = PublicUtility.retrieveJSONBasedOnFileName(jsonConfig.get("groupctoPath") + File.separator +"Locale_CTO_Mapping.json");
					  CtoJSON = (JSONArray) CtoLocaleJson.get(Locale);
				}
				resultMap.put("groupName", (String)testDBJson.get("groupName"));
				String testLocale = (System.getProperty("locale") != null && !(System.getProperty("locale").isEmpty()))?System.getProperty("locale"):(String) testDBJson.get("locale");
				resultMap.put("locale", testLocale);
			}else if("MICROSERVICE".equals((String) testDBJson.get("env"))){
				resultMap.put("env", (String)testDBJson.get("env"));
				resultMap.put("groupName", (String)testDBJson.get("groupName"));
				String testLocale = (System.getProperty("locale") != null && !(System.getProperty("locale").isEmpty()))?System.getProperty("locale"):(String) testDBJson.get("locale");
				resultMap.put("locale", testLocale);
			}else if("PAYUXE".equals((String) testDBJson.get("env"))){
				resultMap.put("env", (String)testDBJson.get("env"));
				resultMap.put("groupName", (String)testDBJson.get("groupName"));
				String testLocale = (System.getProperty("locale") != null && !(System.getProperty("locale").isEmpty()))?System.getProperty("locale"):(String) testDBJson.get("locale");
				resultMap.put("locale", testLocale);
			
		}else if("OPS".equals((String) testDBJson.get("env"))){
			resultMap.put("env", (String)testDBJson.get("env"));
			resultMap.put("groupName", (String)testDBJson.get("groupName"));
			String testLocale = (System.getProperty("locale") != null && !(System.getProperty("locale").isEmpty()))?System.getProperty("locale"):(String) testDBJson.get("locale");
			resultMap.put("locale", testLocale);
		} else if("Integration".equals((String) testDBJson.get("env"))){
			resultMap.put("env", (String)testDBJson.get("env"));
			resultMap.put("groupName", (String)testDBJson.get("groupName"));
			String testLocale = (System.getProperty("locale") != null && !(System.getProperty("locale").isEmpty()))?System.getProperty("locale"):(String) testDBJson.get("locale");
			resultMap.put("locale", testLocale);
		}
			else{
				resultMap.put("env", "OPS");
				resultMap.put("groupName", "OPS");
				resultMap.put("locale", "OPS");
			}
			String groupNam = (String)testDBJson.get("groupName");
			if(("DCG".equals((String) testDBJson.get("env")) ||"LESC".equals((String) testDBJson.get("env"))||"envdata".equals((String) testDBJson.get("env"))) && groupNam.equalsIgnoreCase("ALL")){
				CtoLocaleJson.size();
				for(Object eachgroup:CtoJSON){
					JSONObject testDBJSON = (JSONObject)jsonConfig.get("testDB");
					testDBJSON.put("groupName", eachgroup.toString());
					jsonConfig.put("testDB", testDBJSON);
					serviceTestCaseMap = getServiceTestCaseMappingFromDB(jsonConfig);
					for (final List<String> testScenario : serviceTestCaseMap.values()) {
						testScenarioCount = testScenario.size();
					}
					threadCount = threadCount <= serviceTestCaseMap.size() ? threadCount : serviceTestCaseMap.size();
					threadCount = serviceTestCaseMap.size();
					resultMap.put("testScenarioCount", testScenarioCount);
					resultMap.put("startDtTime", dateFormat.format(new Date()));
					resultMap.put("endDtTime", dateFormat.format(new Date()));
					resultMap.put("updtBy", "itauto1");
					resultMap.put("groupName", eachgroup.toString());
					final String result = PublicUtility.setTestExecutionResult(JSONObject.toJSONString(resultMap));
					suitId = JsonPath.read(result, "$.resultData[0].suitId");
					suitIdArray.add(suitId);
				    suitcountMap.put(suitId, threadCount);
				    suitJsonMap.put(suitId,jsonConfig);
				    suittestscenariocountMap.put(suitId,testScenarioCount);
				    suittestMap.put(suitId,serviceTestCaseMap);
				    suiIdgroupMap.put(suitId,eachgroup.toString());
				    
					
				}
				
			}else{
				resultMap.put("testScenarioCount", testScenarioCount);
				resultMap.put("startDtTime", dateFormat.format(new Date()));
				resultMap.put("endDtTime", dateFormat.format(new Date()));
				resultMap.put("updtBy", "itauto1");
				final String result = PublicUtility.setTestExecutionResult(JSONObject.toJSONString(resultMap));
				suitId = JsonPath.read(result, "$.resultData[0].suitId");
				suitIdArray.add(suitId); 
				suitcountMap.put(suitId, threadCount);
				 suitJsonMap.put(suitId,jsonConfig);
				 suittestscenariocountMap.put(suitId,testScenarioCount);
				 System.out.println("serviceTestCaseMap"+serviceTestCaseMap);
				 System.out.println("suit"+suitId);
				 suittestMap.put(suitId,serviceTestCaseMap);
				
			}
		}
		
		int multiRun = numTests;
		if(System.getProperty("group").toUpperCase().equals("REPORTS")){
			String orderRange = System.getProperty("orderRange");
			if(orderRange != null && !orderRange.isEmpty()){
				multiRun = Integer.parseInt(orderRange);
				if(multiRun > 50){
					threadCount = multiRun/50;
				}
				
			}
		}
		
		//final Date executionStartDate = new Date();
	//ExecutorService executor = null;
	//	 workerList = null;
		if(!isHealthCheck){
		for(Integer eachsuitID: suitIdArray){
		 	ExecutorService executor = Executors.newFixedThreadPool(suitcountMap.get(eachsuitID));
		 	List<Callable<Map<String, String>>> workerList =  new ArrayList<Callable<Map<String, String>>>();
		for (int i = 0; i < numTests; ++i) {
			final JSONObject serviceObj = (JSONObject) serviceArray.get(i);
			  String serviceName = (String) serviceObj.get("service");
			  List<String> testFilePath = suittestMap.get(eachsuitID).get(serviceName);
			  System.out.println(testFilePath);
			if (CollectionUtils.isNotEmpty(testFilePath)) {
				for(int count = 0; count < multiRun; count++){
					Callable<Map<String, String>> worker = new WSWorkerThread(suitJsonMap.get(eachsuitID), serviceObj, testFilePath, startDate, eachsuitID, env, serviceThreadTimeout,suiIdgroupMap);
					workerList.add(worker);
				}
			}
			}
		
	
		// push all failed tests into the map
		
		try {
			List<Future<Map<String, String>>> futureList = executor.invokeAll(workerList);
			for(Future<Map<String, String>> future : futureList) {
				try {
					failedTestsMap.putAll(future.get());
				} catch (ExecutionException e) {
					logger.error("An error has ocurred trying to run the Test suite:");
					e.printStackTrace();
				}
			}
			executor.shutdown();
			executor.awaitTermination(serviceThreadTimeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("An error has ocurred trying to execute the Test suite threads:");
			e.printStackTrace();
		}
		}
		}else{
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		List<Callable<Map<String, String>>> workerList = new ArrayList<Callable<Map<String, String>>>();
		for (int i = 0; i < numTests; ++i) {
			final JSONObject serviceObj = (JSONObject) serviceArray.get(i);
			final String serviceName = (String) serviceObj.get("service");
			final List<String> testFilePath = serviceTestCaseMap.get(serviceName);
			  suiIdgroupMap  = null;
			if (CollectionUtils.isNotEmpty(testFilePath)) {
				for(int count = 0; count < multiRun; count++){
					Callable<Map<String, String>> worker = new WSWorkerThread(jsonConfig, serviceObj, testFilePath, startDate, suitId, env, serviceThreadTimeout,suiIdgroupMap);
					workerList.add(worker);
				}
			}
		}

		// push all failed tests into the map
		try {
			List<Future<Map<String, String>>> futureList = executor.invokeAll(workerList);
			for(Future<Map<String, String>> future : futureList) {
				try {
					failedTestsMap.putAll(future.get());
				} catch (ExecutionException e) {
					logger.error("An error has ocurred trying to run the Test suite:");
					e.printStackTrace();
				}
			}
			executor.shutdown();
			executor.awaitTermination(serviceThreadTimeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("An error has ocurred trying to execute the Test suite threads:");
			e.printStackTrace();
		}
		}
		/*if (BooleanUtils.toBoolean(System.getProperty("health-check")) && startDate != null) {
			final JSONObject healthCheckMaster =  new JSONObject();
			healthCheckMaster.put("env", env);
			healthCheckMaster.put("group", System.getProperty("group"));
			healthCheckMaster.put("locale", System.getProperty("locale"));
			
			final JSONObject healthCheckResult =  new JSONObject();
			healthCheckResult.put("serviceHealthCheckMasterDTO", healthCheckMaster);
			healthCheckResult.put("startedAt", dateFormat.format(startDate));
			//final String response = PublicUtility.setHealthCheckResult(healthCheckResult.toJSONString());
			//logger.info("Health Check Result : " + response);
		}*/
		
		if (isHealthCheck) {
			sendMail(jsonConfig, startDate);
		} else if(!System.getProperty("group").toUpperCase().equals("REPORTS")){
			final double threshold = NumberUtils.toDouble((String) jsonConfig.get("performanceThreshold"));
			final DecimalFormat df = new DecimalFormat("#.###");
			final double totalTime = NumberUtils.toDouble(df.format((System.currentTimeMillis() - start)/1000));
			//ReportUtility.generateReport(threshold, OutputData.processStatistics(), totalTime);
		}
		/*if (StringUtils.isEmpty(env)) {
			final double threshold = NumberUtils.toDouble((String) jsonConfig.get("performanceThreshold"));
			final DecimalFormat df = new DecimalFormat("#.###");
			final double totalTime = NumberUtils.toDouble(df.format((System.currentTimeMillis() - start)/1000));
			ReportUtility.generateReport(threshold, OutputData.processStatistics(), totalTime);
		} else {
			sendMail(jsonConfig, startDate);
		}*/
	}
	
	@SuppressWarnings("unchecked")
	private void sendMail(final JSONObject jsonConfig, final Date startDate) {
		final Properties prop = CommonUtility.getPropertiesFromFile("resources/email.properties");
		final MailInfo mailInfo = new MailInfo();
		
		final String group = System.getProperty("group").toUpperCase();
		final String env = this.env.trim().toUpperCase();
		
		mailInfo.setFrom(prop.getProperty(group + "." + env + ".FROM"));
		
		final String[] to = prop.getProperty(group + "." + env + ".TO").split(",");
		mailInfo.setTo(Arrays.asList(to));
		
		final String[] cc = prop.getProperty(group + "." + env + ".CC").split(",");
		mailInfo.setCc(Arrays.asList(cc));
		
		final String[] bcc = prop.getProperty(group + "." + env + ".BCC").split(",");
		mailInfo.setBcc(Arrays.asList(bcc));
		
		mailInfo.setSubject("Service Health Check - " + group + " | " + env + " | " + startDate);
		
		final JSONObject healthCheckMaster =  new JSONObject();
		healthCheckMaster.put("env", env);
		healthCheckMaster.put("group", group);
		
		final JSONObject healthCheckResult =  new JSONObject();
		healthCheckResult.put("serviceHealthCheckMasterDTO", healthCheckMaster);
		healthCheckResult.put("startedAt", dateFormat.format(startDate));
		final JSONObject result = PublicUtility.getServiceHealthCheckStatus(healthCheckResult.toJSONString());
		
		final VelocityContext context = new VelocityContext();
		context.put("baseURL", (String) jsonConfig.get(env + "-URL"));
		context.put("totalExecute", result != null ? result.get("TotalTestCase") : 0);
		context.put("totalFailed", result != null ? result.get("FailedTestCase") : 0);

		final Template template = Velocity.getTemplate("resources/service-health-check.vm");
		final StringWriter out = new StringWriter();
		template.merge(context, out);
		
		mailInfo.setMessage(out.toString());
		try {
			MailUtil.sendMail(mailInfo);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, List<String>> getServiceTestCaseMappingFromDB(final JSONObject jsonConfig) {
		final JSONArray serviceArray = (JSONArray) jsonConfig.get("testRunOn");
		if (serviceArray == null || serviceArray.size() < 1) {
			logger.error("There is no service defined in config file.");
			return null;
		}
		
		final JSONObject testDBJson = (JSONObject) jsonConfig.get("testDB");

		final Map<String, List<String>> serviceTestCaseMap = new HashMap<String, List<String>>();
		final int numTests = serviceArray.size();
		for (int i = 0; i < numTests; ++i) {
			//final JSONObject serviceObj = (JSONObject) serviceArray.get(i);
			final String serviceName = (System.getProperty("scenarioName") != null && !(System.getProperty("scenarioName").isEmpty()))?System.getProperty("scenarioName"):(String) testDBJson.get("scenarioName");
			String testLocale = (System.getProperty("locale") != null && !(System.getProperty("locale").isEmpty()))?System.getProperty("locale"):(String) testDBJson.get("locale");
			
			if(testLocale.equalsIgnoreCase("ALL")){
				testLocale = "";
			}
			testDBJson.put("scenarioName", serviceName);
			testDBJson.put("locale", testLocale);
			
			final String scenarioNames = PublicUtility.retrieveTestScenarioNamesFromDB(testDBJson.toJSONString());
			
			if (StringUtils.isEmpty(scenarioNames)) {
				logger.error("No test found from DB for \"" + serviceName + "\" scenario.");
				continue;
			}
			
			final JSONObject jsonObj = (JSONObject) PublicUtility.retrieveJSONFromString(scenarioNames);
			
			if (jsonObj != null) {
				final JSONArray scenarioArray = (JSONArray) jsonObj.get("testScenarioNames");
				if (scenarioArray != null && scenarioArray.size() > 0) {
					final int iScenarioArray = scenarioArray.size();
					final List<String> testCaseNameLst = new ArrayList<String>(iScenarioArray);
					
					for (int iCount = 0; iCount < iScenarioArray; ++iCount) {
						testCaseNameLst.add((String) scenarioArray.get(iCount));
					}
					if (CollectionUtils.isNotEmpty(testCaseNameLst)) {
						serviceTestCaseMap.put(serviceName, testCaseNameLst);
						logger.info(testCaseNameLst.size() + " test case found for \"" + serviceName + "\" service.");
					}
				}
			}
		}

		return serviceTestCaseMap;
	}

	private Map<String, List<String>> getServiceTestCaseMapping(final JSONObject jsonConfig) {
		final File[] files = new File((String)jsonConfig.get("testFolder")).listFiles();
		if (ArrayUtils.isEmpty(files)) {
			logger.error("There is no test data in test folder.");
			return null;
		}
		
		final JSONArray serviceArray = (JSONArray) jsonConfig.get("testRunOn");
		if (serviceArray == null || serviceArray.size() < 1) {
			logger.error("There is no service defined in config file.");
			return null;
		}

		final Map<String, List<String>> serviceTestCaseMap = new HashMap<String, List<String>>();
		final int numTests = serviceArray.size();
		for (int i = 0; i < numTests; ++i) {
			final JSONObject serviceObj = (JSONObject) serviceArray.get(i);
			final String serviceName = (String) serviceObj.get("service");
			
			boolean isServiceTestCaseFound = false;
			for (final File file : files) {
				if (file.isDirectory() && file.getName().equals(serviceName)) {
					final File[] testCases = file.listFiles(new FilenameFilter() {
						public boolean accept(final File dir, final String name) {
							if (name.toLowerCase().endsWith("json")) {
								return true;
							} else {
								return false;
							}
						}
					});
					List<String> testCaseNameLst = null;
					if (ArrayUtils.isNotEmpty(testCases)) {
						testCaseNameLst = new ArrayList<String>();
						for (final File testCaseFile : testCases) {
							testCaseNameLst.add(testCaseFile.getPath());
						}
					}
					if (CollectionUtils.isNotEmpty(testCaseNameLst)) {
						serviceTestCaseMap.put(file.getName(), testCaseNameLst);
						isServiceTestCaseFound = true;
						logger.info(testCaseNameLst.size() + " test case found for " + serviceName + " service.");
					}
				}
			}
			if (!isServiceTestCaseFound) {
				logger.error("There is no test case for service: " + serviceName);
			}
		}
		return serviceTestCaseMap;
	}
}