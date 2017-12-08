package com.moto.common.util.validator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import com.jayway.jsonpath.JsonPath;
import com.moto.common.ws.automation.utility.PublicUtility;

/**
 * This validator serves only the purpose of testing this feature usage, and to
 * serve as an example for others developers.
 * 
 * @author eduardof
 * 
 */
public class TestValidatorTest implements Validator {
	private static final Logger logger = LogManager.getLogger(TestValidatorTest.class);

	/**
	 * This validation simply prints in the console that this validatior is
	 * being used. In case parameters were used, they are also printed in the
	 * console.
	 */
	@Override
	public String[] validate(final String[] parameters, final String jsonData) {
		logger.info("Simple dumny test validation perfromed.");
		if (ArrayUtils.isNotEmpty(parameters)) {
			logger.info("The entered parameters are: ");
			for (final String parameter : parameters) {
				logger.info(parameter + " ");
			}
		} else {
			logger.info("No parameters were passed to this method.");
		}
		if (StringUtils.isNotEmpty(jsonData)) {
			Object json = null;
			if (jsonData.trim().startsWith("{") && jsonData.endsWith("}")) {
				json = net.sf.json.JSONObject.fromObject(jsonData);
			} else if (jsonData.trim().startsWith("[") && jsonData.endsWith("]")) {
				json = net.sf.json.JSONArray.fromObject(jsonData);
			}
			
			final JSONObject jsonConfig = PublicUtility.retrieveJSONBasedOnFileName("tests-dtc-adapter/dtc-adapter_config.json");
			final Object oSource = JsonPath.read(jsonConfig, "$.productLineItems[*].warehouseId");
			System.out.println(oSource);
		}
		return null;
	}
}