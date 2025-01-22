package org.foo.modules.jahia.listeners;

import org.apache.commons.collections.CollectionUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.drools.core.util.StringUtils;
import org.jahia.api.Constants;
import org.jahia.api.content.JCRTemplate;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationEvent;
import org.jahia.services.content.PublicationEventListener;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component(service = PublicationEventListener.class, immediate = true)
public class SamplePublicationListener implements PublicationEventListener {
    private static final Logger logger = LoggerFactory.getLogger(SamplePublicationListener.class);

    private static final String FIRST_PUBLICATION_DATE = "firstPublicationDate";
    private static final String MIXIN_PUBLICATION_METADATA = "foomix:publicationMetaData";

    @Reference
    private JCRPublicationService jcrPublicationService;

    @Reference
    private JCRTemplate jcrTemplate;

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
        JCRSessionWrapper jcrSessionWrapper = publicationEvent.getDestinationSession();
        if (Constants.LIVE_WORKSPACE.equals(jcrSessionWrapper.getWorkspace().getName())) {
            logger.info("<<< Start publicationEvent on live workspace");
            Set<String> uuidsToPublish = new HashSet<>();
            publicationEvent.getContentPublicationInfos().forEach(info -> {
                String nodePath = info.getNodePath();
                logger.info("{}Node: {}", StringUtils.repeat(" ", 4), nodePath);
                if (CollectionUtils.isNotEmpty(info.getPublicationLanguages())) {
                    info.getPublicationLanguages().forEach(language -> {
                        logger.info("{}language: {}", StringUtils.repeat(" ", 4 * 2), language);
                        uuidsToPublish.addAll(setFirstPublicationDate(jcrSessionWrapper.getUser(), LanguageCodeConverters.languageCodeToLocale(language), nodePath));
                    });
                } else {
                    logger.info("{}no language", StringUtils.repeat(" ", 4 * 2));
                    uuidsToPublish.addAll(setFirstPublicationDate(jcrSessionWrapper.getUser(), null, nodePath));
                }
            });
            try {
                jcrPublicationService.publish(uuidsToPublish.stream().filter(Objects::nonNull).collect(Collectors.toList()),
                        Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, Collections.singletonList(FIRST_PUBLICATION_DATE));
            } catch (RepositoryException e) {
                logger.error("", e);
            }
            logger.info(">>> End publicationEvent on live workspace");
        }
    }

    private Set<String> setFirstPublicationDate(JahiaUser jahiaUser, Locale locale, String nodePath) {
        try {
            return jcrTemplate.doExecuteWithSystemSessionAsUser(jahiaUser, Constants.EDIT_WORKSPACE, locale, session -> {
                Set<String> uuidsToPublish = new HashSet<>();
                if (session.nodeExists(nodePath)) {
                    JCRNodeWrapper node = session.getNode(nodePath);
                    uuidsToPublish.add(setFirstPublicationDateOnNode(node, session));
                    if (locale != null) {
                        uuidsToPublish.add(setFirstPublicationDateOnNode(node.getOrCreateI18N(locale), session));
                    }
                }
                return uuidsToPublish;
            }).stream().filter(Objects::nonNull).collect(Collectors.toSet());
        } catch (RepositoryException e) {
            logger.error("", e);
        }
        return Collections.emptySet();
    }

    private String setFirstPublicationDateOnNode(Node node, JCRSessionWrapper session) throws RepositoryException {
        if (!node.hasProperty(FIRST_PUBLICATION_DATE)) {
            if (!node.isNodeType(MIXIN_PUBLICATION_METADATA)) {
                node.addMixin(MIXIN_PUBLICATION_METADATA);
            }
            Calendar currentDate = Calendar.getInstance();
            logger.info("{}Set first publication date: {} for node {}", StringUtils.repeat(" ", 4 * 2), ISO8601.format(currentDate), node.getPath());
            node.setProperty(FIRST_PUBLICATION_DATE, currentDate);
            session.save();
            return node.getIdentifier();
        }
        return null;
    }
}
