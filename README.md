# Keycloak 2FA Social Media Authenticator

Keycloak Authentication Provider implementation to get a 2nd-factor authentication with an OTP/code/token sent via social media platforms (e.g., WhatsApp, Signal, Telegram).

This project replaces the traditional SMS-based OTP delivery with a more cost-effective and flexible social media-based approach.

## Features
- Sends OTPs via social media platforms using a custom API.
- Configurable client ID and secret for different social media providers.
- Fully integrated with Keycloak's authentication flow.

## Setup Instructions
1. Configure your social media API endpoint and credentials in the Keycloak admin console.
2. Deploy the authenticator to your Keycloak instance.
3. Assign the authenticator to the desired authentication flow.

## Additional Configuration

Ensure the following configurations are set in the Keycloak admin console:

- **Client ID**: The client ID for the social media OTP service.
- **Client Secret**: The client secret for the social media OTP service.
- **Mobile Number**: Ensure users have a valid mobile number attribute set in their profile.

## Resources
For more details, refer to the official Keycloak documentation or contact the project maintainer.
