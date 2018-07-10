package service.tut.pori.users;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.properties.SystemProperty;

/**
 * Properties for User Service.
 */
public class UserServiceProperties extends SystemProperty {
	private static final String PROPERTY_SERVICE_PORI_USERS = PROPERTY_SERVICE_PORI+".users";
	private static final String PROPERTY_SERVICE_PORI_USERS_REGISTER_PASSWORD = PROPERTY_SERVICE_PORI_USERS+".register_password";
	private String _registerPassword = null;

	@Override
	public void initialize(Properties properties) throws IllegalArgumentException {
		_registerPassword = properties.getProperty(PROPERTY_SERVICE_PORI_USERS_REGISTER_PASSWORD);
		if(StringUtils.isBlank(_registerPassword)){
			Logger.getLogger(getClass()).debug("No register password set.");
		}
	}

	/**
	 * @return the registerPassword
	 */
	public String getRegisterPassword() {
		return _registerPassword;
	}
}
