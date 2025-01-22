package org.foo.modules.jahia.jobs;

import org.jahia.api.Constants;
import org.jahia.api.content.JCRTemplate;
import org.jahia.api.usermanager.JahiaUserManagerService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Component(service = BackgroundSyncService.class)
public class BackgroundSyncService {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundSyncService.class);

    private static final String ROOT_NODE = "/sites/" + JahiaSitesService.SYSTEM_SITE_KEY + "/contents";

    @Reference
    private JCRPublicationService jcrPublicationService;
    @Reference
    private JCRTemplate jcrTemplate;
    @Reference
    private JahiaUserManagerService jahiaUserManagerService;
    @Reference
    private JCRSessionFactory jcrSessionFactory;

    public void executeJahiaJob() throws RepositoryException {
        logger.info("Create and publish node");
        JahiaUser savedUser = jcrSessionFactory.getCurrentUser();
        jcrSessionFactory.setCurrentUser(jahiaUserManagerService.lookupRootUser().getJahiaUser());
        Set<String> uuidsToPublish = new HashSet<>();
        try {
            int index = new Random().nextInt(10);
            for (Locale locale : Arrays.asList(Locale.FRENCH, Locale.ENGLISH)) {
                uuidsToPublish.add(jcrTemplate.doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, locale, session -> {
                    JCRNodeWrapper textNode = getOrCreateNode(session.getNode(ROOT_NODE), index);
                    textNode.setProperty("text", UUID.randomUUID().toString());
                    textNode.saveSession();
                    return textNode.getIdentifier();
                }));
            }

            uuidsToPublish.forEach(uuid -> {
                try {
                    jcrPublicationService.publishByMainId(uuid);
                } catch (RepositoryException e) {
                    logger.error("", e);
                }
            });

            if (!new Random().nextBoolean()) {
                throw new RuntimeException("Fake exception");
            }
        } finally {
            jcrSessionFactory.setCurrentUser(savedUser);
        }
    }

    private JCRNodeWrapper getOrCreateNode(JCRNodeWrapper rootNode, int index) throws RepositoryException {
        if (!rootNode.hasNode("text-" + index)) {
            return rootNode.addNode("text-" + index, "jnt:text");
        }
        return rootNode.getNode("text-" + index);
    }
}
