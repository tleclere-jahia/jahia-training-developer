package org.foo.modules.jahia.rules;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.jahia.api.Constants;
import org.jahia.api.content.JCRTemplate;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCREventIterator;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Component(service = DefaultEventListener.class)
public class ImageConverterListener extends DefaultEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ImageConverterListener.class);

    private JCRTemplate jcrTemplate;

    @Reference
    private void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    @Override
    public int getEventTypes() {
        return Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED;
    }

    @Override
    public String[] getNodeTypes() {
        return new String[]{Constants.JAHIANT_RESOURCE};
    }

    @Override
    public void onEvent(EventIterator eventIterator) {
        Event event;
        JCRSessionWrapper jcrSessionWrapper = ((JCREventIterator) eventIterator).getSession();
        while (eventIterator.hasNext()) {
            try {
                event = eventIterator.nextEvent();
                if (StringUtils.endsWith(event.getPath(), Constants.JCR_CONTENT + "/" + Constants.JCR_DATA)) {
                    logger.info("{}", event);
                    final String nodePath = StringUtils.substringBeforeLast(event.getPath(), "/");
                    jcrTemplate.doExecuteWithSystemSessionAsUser(null, jcrSessionWrapper.getWorkspace().getName(), null, session -> {
                        JCRNodeWrapper contentNode = session.getNode(nodePath);
                        JCRNodeWrapper fileNode = contentNode.getParent();
                        File srcFile = null, destFile = null;
                        InputStream is = null;
                        try {
                            if (fileNode.isNodeType(Constants.JAHIAMIX_IMAGE) && "image/png".equals(contentNode.getPropertyAsString(Constants.JCR_MIMETYPE))) {
                                srcFile = JCRContentUtils.downloadFileContent(fileNode);
                                destFile = new File(srcFile.getParent(), fileNode.getName().replace(".png", ".jpeg"));
                                IMOperation op = new IMOperation();
                                op.addImage();
                                op.addImage();
                                new ConvertCmd().run(op, srcFile.getAbsolutePath(), destFile.getAbsolutePath());

                                is = Files.newInputStream(destFile.toPath());
                                fileNode.getParent().uploadFile(destFile.getName(), is, "image/jpeg");
                                session.save();
                            }
                        } catch (IOException | IM4JavaException | InterruptedException | RepositoryException e) {
                            logger.error("", e);
                        } finally {
                            IOUtils.closeQuietly(is);
                            FileUtils.deleteQuietly(srcFile);
                            FileUtils.deleteQuietly(destFile);
                        }
                        return true;
                    });
                }
            } catch (RepositoryException e) {
                logger.error("", e);
            }
        }
    }
}
