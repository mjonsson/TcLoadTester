package com.siemens.tcloadtester.core;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * The setting class is used for creating objects of setting pair values.
 * 
 */
@XmlType
public final class Setting {
	/**
	 * Name of setting.
	 */
	@XmlAttribute
	public String name = null;
	/**
	 * Value of setting.
	 */
	@XmlAttribute
	public String value = null;
}
