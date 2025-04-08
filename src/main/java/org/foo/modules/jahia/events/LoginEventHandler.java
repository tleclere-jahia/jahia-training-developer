package org.foo.modules.jahia.events;

import org.jahia.api.Constants;
import org.jahia.api.content.JCRTemplate;
import org.jahia.api.usermanager.JahiaUserManagerService;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Calendar;

@Component(service = EventHandler.class, immediate = true, property = EventConstants.EVENT_TOPIC + "=org/jahia/usersgroups/login/LOGIN")
public class LoginEventHandler implements EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoginEventHandler.class);

    @Reference
    private JCRTemplate jcrTemplate;
    @Reference
    private JahiaUserManagerService jahiaUserManagerService;

    @Override
    public void handleEvent(Event event) {
        logger.info("Received OSGi event: {}", event);
        try {
            jcrTemplate.doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, session -> {
                JCRUserNode user = jahiaUserManagerService.lookupUser(((JahiaUser) event.getProperty("user")).getUsername(), session);
                user.setProperty("lastLogin", Calendar.getInstance());
                user.saveSession();
                return null;
            });
        } catch (RepositoryException e) {
            logger.error("", e);
        }
    }
}
