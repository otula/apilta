package service.tut.pori.apilta.sensors.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

/**
 * Answers to the question "what does this task generate?"
 *
 */
@XmlRootElement(name=Definitions.ELEMENT_OUTPUT)
@XmlAccessorType(XmlAccessType.NONE)
public class Output {
	@XmlElement(name = Definitions.ELEMENT_FEATURE)
	private String _feature = null;

	/**
	 * @return the feature
	 * @see #setFeature(String)
	 */
	public String getFeature() {
		return _feature;
	}

	/**
	 * @param feature the feature to set
	 * @see #getFeature()
	 */
	public void setFeature(String feature) {
		_feature = feature;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(Output)
	 */
	protected boolean isValid() {
		return !StringUtils.isBlank(_feature);
	}

	/**
	 * 
	 * @param output
	 * @return false if output is null or invalid
	 */
	public static boolean isValid(Output output) {
		if(output == null){
			return false;
		}else{
			return output.isValid();
		}
	}
}
