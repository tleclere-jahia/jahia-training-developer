package org.foo.modules.jahia.rules;

import org.jahia.api.Constants;
import org.jahia.api.content.JCRTemplate;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.content.rules.BaseBackgroundAction;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

@Component(service = BackgroundAction.class, immediate = true)
public class IncrementAction extends BaseBackgroundAction {
    private static final Logger logger = LoggerFactory.getLogger(IncrementAction.class);

    private JCRTemplate jcrTemplate;

    @Reference
    private void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public IncrementAction() {
        setName("increment");
    }

    @Override
    public void executeBackgroundAction(JCRNodeWrapper node) {
        try {
            final String sitePath = node.getResolveSite().getPath();
            jcrTemplate.doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, node.getSession().getLocale(), systemSession -> {
                JCRNodeWrapper counterNode;
                if (!systemSession.nodeExists(sitePath + "/counter")) {
                    counterNode = systemSession.getNode(sitePath).addNode("counter", "foont:counter");
                } else {
                    counterNode = systemSession.getNode(sitePath + "/counter");
                }
                counterNode.lockAndStoreToken(getName());
                counterNode.setProperty("count", counterNode.getProperty("count").getLong() + 1);
                counterNode.unlock(getName());
                counterNode.saveSession();

                return null;
            });
        } catch (RepositoryException e) {
            logger.error("", e);
        }
    }
}
