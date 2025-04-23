package faisalalali.keycloak.authenticator;

import faisalalali.keycloak.authenticator.gateway.SocialMediaOtpServiceFactory;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;

import java.util.Locale;

/**
 * Authenticator for sending OTPs via social media platforms.
 */
public class SocialMediaOtpAuthenticator implements Authenticator {

    private static final String MOBILE_NUMBER_FIELD = "mobile_number";
    private static final String TPL_CODE = "login-sms.ftl";

    // Updated to use the correct message key and enhanced error handling
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        KeycloakSession session = context.getSession();
        UserModel user = context.getUser();

        String mobileNumber = user.getFirstAttribute(MOBILE_NUMBER_FIELD);
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            context.failureChallenge(AuthenticationFlowError.INVALID_USER,
                context.form().setError("socialAuthSmsNotSent", "Mobile number is missing").createErrorPage(Response.Status.BAD_REQUEST));
            return;
        }

        int length = Integer.parseInt(config.getConfig().getOrDefault("length", "6"));
        int ttl = Integer.parseInt(config.getConfig().getOrDefault("ttl", "300"));

        String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        authSession.setAuthNote(SocialMediaOtpConstants.CODE, code);
        authSession.setAuthNote(SocialMediaOtpConstants.CODE_TTL, Long.toString(System.currentTimeMillis() + (ttl * 1000L)));

        try {
            Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
            Locale locale = session.getContext().resolveLocale(user);
            String authText = theme.getMessages(locale).getProperty("socialAuthText");
            String message = String.format(authText, code, Math.floorDiv(ttl, 60));

            SocialMediaOtpServiceFactory.get(config.getConfig()).send(mobileNumber, message);

            context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(TPL_CODE));
        } catch (Exception e) {
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                context.form().setError("socialAuthSmsNotSent", e.getMessage()).createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst("code");

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String code = authSession.getAuthNote("code");
        String ttl = authSession.getAuthNote("ttl");

        if (code == null || ttl == null) {
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
            return;
        }

        boolean isValid = enteredCode.equals(code);
        if (isValid) {
            if (Long.parseLong(ttl) < System.currentTimeMillis()) {
                // expired
                context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                    context.form().setError("smsAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
            } else {
                // valid
                context.success();
            }
        } else {
            // invalid
            AuthenticationExecutionModel execution = context.getExecution();
            if (execution.isRequired()) {
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                    context.form().setAttribute("realm", context.getRealm())
                        .setError("smsAuthCodeInvalid").createForm(TPL_CODE));
            } else if (execution.isConditional() || execution.isAlternative()) {
                context.attempted();
            }
        }
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.getFirstAttribute(MOBILE_NUMBER_FIELD) != null;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // this will only work if you have the required action from here configured:
        // https://github.com/dasniko/keycloak-extensions-demo/tree/main/requiredaction
        user.addRequiredAction("mobile-number-ra");
    }

    @Override
    public void close() {
    }

}
