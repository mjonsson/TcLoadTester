package com.siemens.util;

import java.io.InputStream;
import java.util.Properties;


public class PropertiesHelper {
	Properties properties = new Properties();
	
	public static Properties loadProperties(String absoluteName) throws Exception {
		InputStream inputStream = null;
		Properties properties = new Properties();
		
		try {
			inputStream = PropertiesHelper.class.getResourceAsStream(absoluteName);
			properties.load(inputStream);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				inputStream.close();
			}
			catch (Exception e) { }
		}
		
		return properties;
	}
}
