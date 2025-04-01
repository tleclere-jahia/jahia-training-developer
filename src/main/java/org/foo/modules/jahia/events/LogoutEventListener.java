package org.foo.modules.jahia.events;

import org.jahia.api.Constants;
import org.jahia.api.usermanager.JahiaUserManagerService;
import org.jahia.community.aws.cognito.api.AwsCognitoConfiguration;
import org.jahia.community.aws.cognito.api.AwsCognitoConstants;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUserGlobalSignOutRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUserGlobalSignOutResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component(service = EventHandler.class, immediate = true, property = EventConstants.EVENT_TOPIC + "=org/jahia/usersgroups/login/LOGOUT")
public class LogoutEventListener implements EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(LogoutEventListener.class);

    private JahiaUserManagerService jahiaUserManagerService;

    @Reference
    private void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    @Override
    public void handleEvent(Event event) {
        logger.info("Received OSGi event: {}", event);
        HttpServletRequest httpServletRequest = (HttpServletRequest) event.getProperty("request");
        HttpServletResponse httpServletResponse = (HttpServletResponse) event.getProperty("response");
        JahiaUser jahiaUser = (JahiaUser) httpServletRequest.getSession().getAttribute(Constants.SESSION_USER);
        if (jahiaUser == null) {
            logger.error("User not found");
        } else {
            JCRUserNode jcrUserNode = jahiaUserManagerService.lookupUserByPath(jahiaUser.getLocalPath());
            String username = jcrUserNode.getName();
            logger.info("Try to log out user: {}", username);
            AwsCognitoConfiguration awsCognitoConfiguration = AwsCognitoConstants.getAwsCognitoConfiguration(httpServletRequest);
            try (CognitoIdentityProviderClient cognitoIdentityProviderClient = CognitoIdentityProviderClient.builder()
                    .region(Region.of(awsCognitoConfiguration.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                            awsCognitoConfiguration.getAccessKeyId(), awsCognitoConfiguration.getSecretAccessKey())))
                    .build()) {
                AdminUserGlobalSignOutRequest request = AdminUserGlobalSignOutRequest.builder()
                        .userPoolId(awsCognitoConfiguration.getUserPoolId())
                        .username(username)
                        .build();
                AdminUserGlobalSignOutResponse response = cognitoIdentityProviderClient.adminUserGlobalSignOut(request);
                if (response != null && response.sdkHttpResponse() != null && response.sdkHttpResponse().isSuccessful()) {
                    logger.info("User {} successfully logged out.", username);
                } else {
                    logger.error("User {} unsuccessfully logged out.", username);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}", response);
                    }
                }
            }
        }
    }
}
