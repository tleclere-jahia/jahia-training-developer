package org.foo.modules.jahia.jobs;

import org.jahia.api.Constants;
import org.jahia.api.content.JCRTemplate;
import org.jahia.api.usermanager.JahiaUserManagerService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

@Component(service = BackgroundSyncService.class)
public class BackgroundSyncService {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundSyncService.class);
    private static final String ROOT_NODE = "/sites/systemsite/contents";

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
        try {
            jcrTemplate.doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, Locale.FRENCH, session -> {
                JCRNodeWrapper rootNode = session.getNode(ROOT_NODE);
                JCRNodeWrapper textNode = rootNode.addNode(JCRContentUtils.findAvailableNodeName(rootNode, "text"), "jnt:text");
                textNode.setProperty("text", UUID.randomUUID().toString());
                textNode.saveSession();

                jcrPublicationService.publishByMainId(textNode.getIdentifier());
                return null;
            });

            if (!new Random().nextBoolean()) {
                throw new RuntimeException("Fake exception");
            }
        } finally {
            jcrSessionFactory.setCurrentUser(savedUser);
        }
    }
}
