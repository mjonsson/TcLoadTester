package com.siemens.tcloadtester.core;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Datamodel object representing the module occurrence in a worker definition.
 * 
 */
/**
 * @author TMGxmsj
 *
 */
public final class ModuleOcc extends ApplicationObject {
	/**
	 * Id referencing a module id.
	 */
	@XmlAttribute(name = "ref")
	public String refid = null;

	/**
	 * Can have a value of "start", "end" or "false" (Default false). Defines if
	 * the module occurrence in a sequence shall only be started once in the
	 * start or end or if should be run every iteration.
	 */
	@XmlAttribute(name = "once")
	public String runonce = "false";

	/**
	 * Can be a positive integer between 0 and 100. Represents the probability
	 * from 0% to 100% that the module occurrence should be run each iteration.
	 */
	@XmlAttribute(name = "probability")
	public int probability = 100;

	/**
	 * The worker thread instance of the module. The module needs to be cloned
	 * in order to make the application thread-safe.
	 */
	public Module moduleObj = null;
}
