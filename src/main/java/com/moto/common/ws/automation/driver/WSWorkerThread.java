package com.moto.common.ws.automation.driver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

public class WSWorkerThread implements Callable<Map<String, String>> {
	private static final Logger logger = LogManager.getLogger(WSWorkerThread.class);
	
	private JSONObject jsonConfig;
	private JSONObject serviceObj;
	private List<String> testFilePathLst;
	private Date executionStartDate;
	private Integer suitId;
	private String env;
	private HashMap<Integer,String> suiIdgroupMap;
	// Map containing the failed tests
	private Map<String, String> failedTestsMap;
	// Long containing the thread await timeout
	private Long serviceThreadTimeout;

	public WSWorkerThread(JSONObject jsonConfig, JSONObject serviceObj, List<String> testFilePathLst, Date executionStartDate, Integer suitId, String env, Long serviceThreadTimeout, HashMap<Integer,String> suiIdgroupMap) {
		this.jsonConfig = jsonConfig;
		this.serviceObj = serviceObj;
		this.testFilePathLst = testFilePathLst;
		this.executionStartDate = executionStartDate;
		this.suitId = suitId;
		this.env = env;
		this.failedTestsMap = new HashMap<String, String>();
		this.serviceThreadTimeout = serviceThreadTimeout;
		this.suiIdgroupMap = suiIdgroupMap;
	}

	@Override
	public Map<String, String> call() throws Exception {
		this.run();
		return failedTestsMap;
	}
	
	public void run() {
		logger.info(Thread.currentThread().getName() + " Start. Command = " + serviceObj.toString());
		process();
		logger.info(Thread.currentThread().getName() + " End.");
	}
	
	private void process() {
		logger.info("Name: " + serviceObj.get("service") + ", Thread Count : " + serviceObj.get("concurrentThreadCount"));
		int threadCount = NumberUtils.toInt((String) serviceObj.get("concurrentThreadCount"));
		
		if (threadCount < 1) {
			logger.error("Thread count is not greater than 0 or non numeric for " + serviceObj.get("service") + " service.");
			return;
		}
		if (CollectionUtils.isNotEmpty(testFilePathLst)) {
			threadCount = threadCount <= testFilePathLst.size() ? threadCount : testFilePathLst.size();
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			
			List<Callable<Map<String, String>>> workerList = new ArrayList<Callable<Map<String,String>>>();
			for (final String testFilePath : testFilePathLst) {
				Callable<Map<String, String>> worker = new WSTestCaseWorkerThread(jsonConfig, serviceObj, testFilePath, executionStartDate, suitId, env,  suiIdgroupMap );
				workerList.add(worker);
			}

			// include all failed tests into the map
			try {
				List<Future<Map<String, String>>> futureList = executor.invokeAll(workerList);
				for(Future<Map<String, String>> future : futureList) {
					try {
						this.failedTestsMap.putAll(future.get());
					}
					catch (ExecutionException e) {
						logger.error("An error has ocurred trying to run the Test:");
						e.printStackTrace();
					}
				}

				executor.shutdown();
				executor.awaitTermination(serviceThreadTimeout, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				logger.error("An error has ocurred trying to execute the Test threads:");
				e.printStackTrace();
			}
		}
	}
}