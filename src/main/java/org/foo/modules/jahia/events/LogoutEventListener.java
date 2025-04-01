package org.foo.modules.jahia.events;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = EventHandler.class, immediate = true, property = EventConstants.EVENT_TOPIC + "=org/jahia/usersgroups/login/LOGOUT")
public class LogoutEventListener implements EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(LogoutEventListener.class);

    @Override
    public void handleEvent(Event event) {
        logger.info("Received OSGi event: {}", event);
    }
}
