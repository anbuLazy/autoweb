package com.moto.common.ws.automation.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestRestClientUtility {
	
	private static final Logger logger = LogManager.getLogger(TestRestClientUtility.class);

	private static RestClientUtility restClientUtility = null;
	
	@BeforeClass 
	public static void init() {
		logger.info("Calling init()");
		try {
			restClientUtility = new RestClientUtility("http://100.64.140.213:8080/auto-moto-services");
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testRestService() {
		logger.info("Calling testRestService()");
		// Test service
		logger.info(restClientUtility.test());
	}
	
	@Test
	public void getTestScenarioName() {
		logger.info("Calling getTestScenarioName()");
		// Test service
		logger.info(restClientUtility.getTestScenarioName("{\"envUrl\":\"OPS\",\"locale\":\"OPS\",\"scenarioName\":\"workflow\",\"testScenarioName\":\"\",\"groupName\":\"OPS\"}"));
	}
	
	@Test
	public void getDataRestService() {
		logger.info("Calling getDataRestService()");
		// Test service
		//logger.info(restClientUtility.getTestData("{\"envUrl\":\"\",\"locale\":\"\",\"scenarioName\":\"\",\"testScenarioName\":\"\",\"groupName\":\"automated-pricescraping\",\"pageNumber\":1,\"maxRecords\":5}"));
		logger.info(restClientUtility.getTestData("{\"envUrl\":\"OPS\",\"locale\":\"OPS\",\"scenarioName\":\"workflow\",\"testScenarioName\":\"\",\"groupName\":\"OPS\"}"));
	}
	
	@Test
	public void getScenarioNamesRestService() {
		logger.info("Calling getScenarioNamesRestService()");
		// Test service
		final long startTime = System.currentTimeMillis();
		logger.info(restClientUtility.getTestScenarioNames("{\"envUrl\":\"OPS\",\"locale\":\"OPS\",\"scenarioName\":\"workflow\",\"testScenarioName\":\"\",\"groupName\":\"OPS\"}"));
		logger.info(System.currentTimeMillis() - startTime);
	}

	//public static void main(String[] args) {
		// Test service
		/*System.out.println(restClientUtility.test());

		// Get test data, json parameter as string
		System.out.println(restClientUtility.getTestData("{\"envUrl\":\"\",\"locale\":\"\",\"scenarioName\":\"\",\"testScenarioNum\":\"\",\"groupName\":\"automated-pricescraping\",\"pageNumber\":1,\"maxRecords\":5}"));
*/
		// Add test data, json parameter as string
		/*
		restClientUtility.setTestData("{\"env\":\"PROD_UK\",\"locale\":\"en_GB\",\"scenarioName\":\"BuyMotoE\",\"testScenarioNum\":"
				+ new Random().nextInt(10)
				+ ",\"groupName\":\"automated-pricescraping\",\"testData\":\"UnitTestPage,[Motorola Moto X+1 UK|Motorola Moto E 4G 2nd|Moto G 4G SIM-Free|Motorola Moto G 4G 5-Inch 2nd|Moto G 4G SIM-Free|Motorola Google Nexus 6 32GB|Moto 360],TescoMotorolaPage,[Moto E 2nd Generation|Moto E|Moto G 2nd Generation|Moto G with 4G],CarPhoneWarehouseMobilesPage,[Motorola Moto G Dual SIM (2nd gen)|Motorola Moto G 4G (2nd gen)|Nexus 6 32GB|Motorola Moto 360]\",\"updatedBy\":\"Rest Client\",\"testDataType\":\"csv\"}");
*/
		// Edit test data, json parameter as string
		/*
		restClientUtility.setTestData("{\"testId\":155,\"env\":\"PROD_UK\",\"locale\":\"en_GB\",\"scenarioName\":\"BuyMotoE\",\"testScenarioNum\":"
				+ new Random().nextInt(10)
				+ ",\"groupName\":\"automated-pricescraping\",\"testData\":\"RestClient,[Motorola Moto X+1 UK|Motorola Moto E 4G 2nd|Moto G 4G SIM-Free|Motorola Moto G 4G 5-Inch 2nd|Moto G 4G SIM-Free|Motorola Google Nexus 6 32GB|Moto 360],TescoMotorolaPage,[Moto E 2nd Generation|Moto E|Moto G 2nd Generation|Moto G with 4G],CarPhoneWarehouseMobilesPage,[Motorola Moto G Dual SIM (2nd gen)|Motorola Moto G 4G (2nd gen)|Nexus 6 32GB|Motorola Moto 360]\",\"updatedBy\":\"Rest Client\",\"testDataType\":\"csv\"}");
*/
	//}
}