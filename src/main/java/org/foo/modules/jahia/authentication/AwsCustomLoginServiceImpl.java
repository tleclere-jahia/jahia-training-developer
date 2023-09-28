package org.foo.modules.jahia.authentication;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.usermanager.JahiaUserManagerService;
import org.jahia.community.aws.cognito.api.AwsCognitoConfiguration;
import org.jahia.community.aws.cognito.api.AwsCognitoConstants;
import org.jahia.community.aws.cognito.api.AwsCustomLoginService;
import org.jahia.community.aws.cognito.provider.AwsCognitoCacheManager;
import org.jahia.osgi.BundleUtils;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

@Component(service = AwsCustomLoginService.class, property = Constants.SERVICE_RANKING + ":Integer=1")
public class AwsCustomLoginServiceImpl implements AwsCustomLoginService {
    private static final Logger logger = LoggerFactory.getLogger(AwsCustomLoginServiceImpl.class);

    private JahiaUserManagerService jahiaUserManagerService;

    @Reference
    private void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    @Override
    public Response login(String userIdentifier, HttpServletRequest httpServletRequest, String siteKey, AwsCognitoConfiguration awsCognitoConfiguration) {
        logger.info("new AWS cognito service");
        JCRUserNode jcrUserNode = jahiaUserManagerService.lookupUser(userIdentifier);
        if (jcrUserNode != null) {
            logger.info("Log in the user {}: {}", jcrUserNode, jcrUserNode.getJahiaUser().getProperties());

            JahiaUser jahiaUser = jcrUserNode.getJahiaUser();
            httpServletRequest.getSession().invalidate();
            // user has been successfully authenticated, note this in the current session.
            httpServletRequest.getSession().setAttribute(org.jahia.api.Constants.SESSION_USER, jahiaUser);
            httpServletRequest.setAttribute(LoginEngineAuthValveImpl.VALVE_RESULT, LoginEngineAuthValveImpl.OK);

            return Response.seeOther(URI.create("/")).build();

            /* String familyName = jcrUserNode.getPropertyAsString(AwsCognitoConstants.USER_PROPERTY_LASTNAME);
            if (familyName != null) {
                try (CognitoIdentityProviderClient cognitoIdentityProviderClient = CognitoIdentityProviderClient.builder()
                        .region(Region.of(awsCognitoConfiguration.getRegion()))
                        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                                awsCognitoConfiguration.getAccessKeyId(), awsCognitoConfiguration.getSecretAccessKey())))
                        .build()) {
                    AdminUpdateUserAttributesRequest request = AdminUpdateUserAttributesRequest.builder()
                            .userPoolId(awsCognitoConfiguration.getUserPoolId())
                            .username(jcrUserNode.getName())
                            .userAttributes(AttributeType.builder()
                                    .name("custom_attribute").value(UUID.randomUUID().toString())
                                    .build())
                            .build();
                    AdminUpdateUserAttributesResponse response = cognitoIdentityProviderClient.adminUpdateUserAttributes(request);
                    if (response != null && response.sdkHttpResponse() != null && response.sdkHttpResponse().isSuccessful()) {
                        BundleUtils.getOsgiService(AwsCognitoCacheManager.class, null).flushCaches();
                        String returnUrl = (String) httpServletRequest.getSession(false).getAttribute(AwsCognitoConstants.SESSION_OAUTH_AWS_COGNITO_RETURN_URL);
                        if (returnUrl == null) {
                            returnUrl = "/";
                        }
                        return Response.seeOther(URI.create(returnUrl)).build();
                    }
                }
            }

            AwsCognitoConfiguration awsCognitoConfiguration = AwsCognitoConstants.getAwsCognitoConfiguration(req);
            try (CognitoIdentityProviderClient cognitoIdentityProviderClient = CognitoIdentityProviderClient.builder()
                    .region(Region.of(awsCognitoConfiguration.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsCognitoConfiguration.getAccessKeyId(), awsCognitoConfiguration.getSecretAccessKey())))
                    .build()) {
            } */
        }

        logger.warn("Login failed (user {} not found in JCR).", userIdentifier);
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
