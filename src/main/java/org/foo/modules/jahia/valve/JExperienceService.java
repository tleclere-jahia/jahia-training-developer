package org.foo.modules.jahia.valve;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.unomi.api.ContextRequest;
import org.apache.unomi.api.CustomItem;
import org.apache.unomi.api.Event;
import org.apache.unomi.api.Profile;
import org.jahia.api.Constants;
import org.jahia.modules.jexperience.admin.ContextServerService;
import org.jahia.services.content.decorator.JCRUserNode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component(service = JExperienceService.class, immediate = true)
public class JExperienceService {
    private static final Logger logger = LoggerFactory.getLogger(JExperienceService.class);

    private static final String CUSTOM_IDENTIFIER = "customIdentifier";

    private ContextServerService contextServerService;

    @Reference
    private void setContextServerService(ContextServerService contextServerService) {
        this.contextServerService = contextServerService;
    }

    public void sendLoginEvent(JCRUserNode userNode, String customData, String siteKey, HttpServletRequest request) throws RepositoryException, JsonProcessingException {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put(Constants.NODENAME, userNode.getName());
        userProperties.put(CUSTOM_IDENTIFIER, userNode.getIdentifier() + "-" + customData);
        request.getParameterMap().forEach((key, value) -> {
            if (CollectionUtils.size(value) == 1) {
                userProperties.put(key, value[0]);
            } else {
                userProperties.put(key, value);
            }
        });
        updateUser(userNode, siteKey, request, userProperties);
    }

    private void updateUser(JCRUserNode userNode, String siteKey, HttpServletRequest request, Map<String, Object> userProperties) throws JsonProcessingException {
        String profileId = contextServerService.getProfileId(request, siteKey);
        CustomItem target = new CustomItem((String) userProperties.get(CUSTOM_IDENTIFIER), "jahiaUser");
        target.setProperties(userProperties);
        Event loginEvent = new Event("login", null, new Profile(profileId), siteKey, null, target, new Date());

        ContextRequest contextRequest = new ContextRequest();
        CustomItem source = new CustomItem("/sites/" + siteKey, "site");
        source.setScope(siteKey);
        contextRequest.setSource(source);
        contextRequest.setRequireSegments(false);
        contextRequest.setEvents(Collections.singletonList(loginEvent));
        contextRequest.setRequiredProfileProperties(Collections.singletonList("*"));

        ObjectMapper mapper = new ObjectMapper();
        final String req = mapper.writeValueAsString(contextRequest);
        logger.debug("Login event body = " + req);

        final AsyncHttpClient asyncHttpClient = contextServerService.initAsyncHttpClient(siteKey);
        AsyncHttpClient.BoundRequestBuilder rb = contextServerService.initAsyncRequestBuilder(siteKey, asyncHttpClient, "/context.json?sessionId=" + contextServerService.getWemSessionId(request), false, false, true);
        rb.setHeader("Content-Type", "application/json");
        if (profileId != null) {
            rb.setHeader("Cookie", "context-profile-id=" + profileId);
            logger.debug("Login event cookie header context-profile-id set");
        }
        rb.setBody(req).execute(new AsyncCompletionHandler<Response>() {
            @Override
            public void onThrowable(Throwable t) {
                asyncHttpClient.closeAsynchronously();
            }

            @Override
            public Response onCompleted(Response response) {
                logger.debug("Login event response code = " + response.getStatusCode());
                if (response.getStatusCode() >= 400) {
                    logger.error(Integer.toString(response.getStatusCode()));
                    logger.error(response.getStatusText());
                }
                asyncHttpClient.closeAsynchronously();
                return response;
            }
        });
    }
}
