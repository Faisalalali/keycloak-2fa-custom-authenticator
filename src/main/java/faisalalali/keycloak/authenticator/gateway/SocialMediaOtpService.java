package faisalalali.keycloak.authenticator.gateway;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Service to send OTPs via social media platforms using a custom API endpoint.
 */
public class SocialMediaOtpService implements CustomService {

    private final String clientId;
    private final String clientSecret;

    public SocialMediaOtpService(Map<String, String> config) {
        this.clientId = config.get("clientId");
        this.clientSecret = config.get("clientSecret");
    }

    @Override
    public void send(String phoneNumber, String message) {
        try {
            URL url = new URL("https://social-auth.fai.ad");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String payload = String.format(
                "{\"PHONE_NUMBER\":\"%s\",\"CLIENT_ID\":\"%s\",\"CLIENT_SECRET\":\"%s\"}",
                phoneNumber, clientId, clientSecret
            );

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed to send OTP: HTTP error code " + responseCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while sending OTP via social media service", e);
        }
    }
}
