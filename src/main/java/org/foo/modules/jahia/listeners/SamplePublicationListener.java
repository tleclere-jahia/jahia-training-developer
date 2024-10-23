package org.foo.modules.jahia.listeners;

import org.apache.commons.collections.CollectionUtils;
import org.drools.core.util.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.PublicationEvent;
import org.jahia.services.content.PublicationEventListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = PublicationEventListener.class, immediate = true)
public class SamplePublicationListener implements PublicationEventListener {
    private static final Logger logger = LoggerFactory.getLogger(SamplePublicationListener.class);

    @Reference
    private JCRPublicationService jcrPublicationService;

    @Activate
    private void start() {
        jcrPublicationService.registerListener(this);
    }

    @Deactivate
    private void stop() {
        jcrPublicationService.unregisterListener(this);
    }

    @Override
    public void onPublicationCompleted(PublicationEvent publicationEvent) {
        if (Constants.LIVE_WORKSPACE.equals(publicationEvent.getDestinationSession().getWorkspace().getName())) {
            logger.info("<<< Start publicationEvent on live workspace");
            publicationEvent.getContentPublicationInfos().forEach(info -> {
                logger.info("{}Node: {}", StringUtils.repeat(" ", 4), info.getNodePath());
                if (CollectionUtils.isNotEmpty(info.getPublicationLanguages())) {
                    info.getPublicationLanguages().forEach(language -> {
                        logger.info("{}language: {}", StringUtils.repeat(" ", 4 * 2), language);
                    });
                }
            });
            logger.info(">>> End publicationEvent on live workspace");
        }
    }
}
