package com.moto.common.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

public class CommonUtility {
	public static Properties getPropertiesFromFile(final String filePath) {
		try (final InputStream in = new FileInputStream(filePath)) {
	        final Properties prop = new Properties();
	        prop.load(in);
	        return prop;
		} catch (IOException e) {
            e.printStackTrace();
        }
		return null;
	}
	
	public static int generateNum(){
		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt();
		return randomInt;
	}
	
}