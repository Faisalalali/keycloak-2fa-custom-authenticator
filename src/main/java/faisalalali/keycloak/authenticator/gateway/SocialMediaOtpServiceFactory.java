package faisalalali.keycloak.authenticator.gateway;

import faisalalali.keycloak.authenticator.SocialMediaOtpConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Factory to create instances of SmsService based on configuration.
 */
@Slf4j
public class SocialMediaOtpServiceFactory {

    public static CustomService get(Map<String, String> config) {
        if (config.containsKey("clientId") && config.containsKey("clientSecret")) {
            return new SocialMediaOtpService(config);
        } else {
            throw new IllegalArgumentException("Invalid configuration for Social Media OTP Service");
        }
    }
}
