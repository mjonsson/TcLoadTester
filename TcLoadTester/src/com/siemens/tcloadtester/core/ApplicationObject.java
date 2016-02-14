package com.siemens.tcloadtester.core;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.siemens.util.Pair;

/**
 * Top class for all defined objects in the xml-configuration that have an id
 * and a list of settings.
 * 
 */
public abstract class ApplicationObject {
	/**
	 * Reg ex. for finding the RandNr construct within the xml-configuration
	 * file.
	 */
	private static final String regexNr = "\\[\\s*RandNr\\s*\\(\\s*(\\d+\\s*,\\s*\\d+)\\s*\\)\\s*\\]";
	/**
	 * Reg ex. for finding the RandOpt construct within the xml-configuration
	 * file.
	 */
	private static final String regexOpt = "\\[\\s*RandOpt\\s*\\(\\s*(.+)\\s*\\)\\s*\\]";
	/**
	 * The compiled pattern for RandNr.
	 */
	private static final Pattern pNr = Pattern.compile(regexNr);
	/**
	 * The compiled pattern for RandOpt.
	 */
	private static final Pattern pOpt = Pattern.compile(regexOpt);
	/**
	 * Initializes a new Random object with a seed.
	 */
	protected static final Random rnd = new Random(System.currentTimeMillis());
	/**
	 * Sets the format of the dateformat object.
	 */
//	private static final DateFormat dateFormat = new SimpleDateFormat(
//			"yy/MM/dd HH:mm:ss");

	/**
	 * Property representing global unique id for all objects
	 */
	@XmlAttribute(name = "id")
	protected String id;
	/**
	 * A list of object level settings.
	 */
	@XmlElementWrapper(name = "settings")
	@XmlElement(name = "setting")
	protected Setting[] settingsList;

	public ApplicationObject() {
	}

	/**
	 * Copy constructor.
	 * 
	 * @param copy
	 *            Source object.
	 */
//	public ApplicationObject(ApplicationObject copy) {
//		this.id = copy.id;
//		if (copy.settingsList != null)
//			this.settingsList = copy.settingsList.clone();
//	}

//	/**
//	 * Constructor based on object id and a settings list.
//	 * 
//	 * @param id
//	 *            Global id. of the application object.
//	 * @param settingsList
//	 *            Object level settings list.
//	 */
//	public ApplicationObject(String id, Setting[] settingsList) {
//		this.id = id;
//		if (settingsList != null)
//			this.settingsList = settingsList.clone();
//	}

	public final String getId() {
		return id;
	}

	protected final boolean settingExist(String name) {
		if (settingsList != null)
			for (Setting s : settingsList)
				if (name.equals(s.name))
					return true;
		if (Application.staticGlobalsList != null)
			for (Setting s : Application.staticGlobalsList)
				if (name.equals(s.name))
					return true;
		
		return false;
	}
	
	/**
	 * Convenience method that returns the local object setting for a specific
	 * input parameter. If no local setting exists, it fallbacks to the global
	 * settings.
	 * 
	 * @param name
	 *            Name of setting to retrieve.
	 * @return The value of the setting as a string.
	 * @throws Exception
	 */
	protected final String getSetting(String name) throws Exception {
		if (settingsList != null)
			for (Setting s : settingsList)
				if (name.equals(s.name))
					return s.value;
		if (Application.staticGlobalsList != null)
			for (Setting s : Application.staticGlobalsList)
				if (name.equals(s.name))
					return s.value;

		throw new Exception(String.format("Can not find setting \"%s\"", name));
	}

	/**
	 * Convenience method for getting a parsed setting.
	 * 
	 * @param name
	 *            Name of setting
	 * @return The parsed string
	 * @throws Exception
	 */
	protected final String getParsedSetting(String name) throws Exception {
		return parseValue(getSetting(name));
	}

	/**
	 * Convenience method for getting a setting as a boolean type.
	 * 
	 * @param value
	 *            Name of setting.
	 * @return A boolean representation.
	 * @throws Exception
	 */
	protected final boolean getSettingAsBool(String name) throws Exception {
		if (getSetting(name).toLowerCase().matches("true|1|y")) {
			return true;
		}

		return false;
	}

	/**
	 * Convenience method for getting a setting as a long type.
	 * 
	 * @param value
	 *            Name of setting.
	 * @return A long representation.
	 * @throws Exception
	 */
	protected final long getSettingAsLong(String name) throws Exception {
		return Long.parseLong(getSetting(name));
	}

	/**
	 * Convenience method for getting a setting as a integer type.
	 * 
	 * @param value
	 *            Name of setting.
	 * @return A integer representation.
	 * @throws Exception
	 */
	protected final int getSettingAsInt(String name) throws Exception {
		return Integer.parseInt(getSetting(name));
	}

	/**
	 * Convenience method for getting a parsed setting as a long type.
	 * 
	 * @param value
	 *            Name of setting.
	 * @return A long representation.
	 * @throws Exception
	 */
	protected final long getParsedSettingAsLong(String name) throws Exception {
		return Long.parseLong(getParsedSetting(name));
	}

	/**
	 * Convenience method for getting a parsed setting as a integer type.
	 * 
	 * @param value
	 *            Name of setting.
	 * @return A integer representation.
	 * @throws Exception
	 */
	protected final int getParsedSettingAsInt(String name) throws Exception {
		return Integer.parseInt(getParsedSetting(name));
	}

	/**
	 * Convenience method that parses a formatted string against a set of
	 * pre-defined regular expressions.
	 * 
	 * @param value
	 *            The string to be parsed.
	 * @return The parsed value.
	 * @throws Exception
	 */
	private static final String parseValue(String value) throws Exception {
		Matcher mNr = pNr.matcher(value);

		// Parse for "RandNr" statement
		while (mNr.find()) {
			int upperBounds = Integer.parseInt(mNr.group(1).split(",")[1]
					.trim());
			int lowerBounds = Integer.parseInt(mNr.group(1).split(",")[0]
					.trim());
			String random = Integer.toString(rnd.nextInt(upperBounds - lowerBounds) + lowerBounds);
			value = mNr.replaceFirst(random);
			mNr.reset(value);
		}

		Matcher mOpt = pOpt.matcher(value);

		// Parse for "RandOpt" statement
		while (mOpt.find()) {
			String[] options = mOpt.group(1).split(",");
			String random = options[rnd.nextInt(options.length)].trim();
			value = mOpt.replaceFirst(random);
			mOpt.reset(value);
		}

		return value;
	}

//	/**
//	 * Convenience method that returns a formatted date.
//	 * 
//	 * @return A string representing the date.
//	 * @throws Exception
//	 */
//	protected static final synchronized String getDate() {
//		return dateFormat.format(new Date());
//	}

	protected final void validate(String[] properties) {
		for (String property : properties) {
			try {
				getSetting(property);
			}
			catch (Exception e) {
				Application.propertyValidation.add(Pair.of(id, property));
			}
		}
	}
}