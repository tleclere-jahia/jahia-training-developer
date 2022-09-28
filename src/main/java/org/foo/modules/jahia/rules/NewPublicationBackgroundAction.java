package org.foo.modules.jahia.rules;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.BackgroundAction;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = BackgroundAction.class)
public class NewPublicationBackgroundAction implements BackgroundAction {
    private static final Logger logger = LoggerFactory.getLogger(NewPublicationBackgroundAction.class);

    @Override
    public String getName() {
        return "newpublication";
    }

    @Override
    public void executeBackgroundAction(JCRNodeWrapper jcrNodeWrapper) {
        logger.info("A new node is published: {}", jcrNodeWrapper);
    }
}
