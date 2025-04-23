package faisalalali.keycloak.authenticator.gateway;

import java.util.Map;

/**
 * Service interface for sending OTPs via custom channels.
 */
public interface CustomService {

	void send(String phoneNumber, String message);

}
