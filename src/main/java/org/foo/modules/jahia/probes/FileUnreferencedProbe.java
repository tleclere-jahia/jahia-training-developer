package org.foo.modules.jahia.probes;

import org.jahia.api.Constants;
import org.jahia.api.content.JCRTemplate;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component(service = Probe.class)
public class FileUnreferencedProbe implements Probe {
    private static final Logger logger = LoggerFactory.getLogger(FileUnreferencedProbe.class);

    @Reference
    private JCRTemplate jcrTemplate;

    @Override
    public String getName() {
        return "Files unreferenced";
    }

    @Override
    public String getDescription() {
        return "List files unreferenced to clean datastore";
    }

    @Override
    public ProbeStatus getStatus() {
        long nbFiles = 0;
        try {
            nbFiles = jcrTemplate.doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, session -> {
                long nbFilesUnreferenced = 0;
                JCRNodeIteratorWrapper it = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [nt:file]", Query.JCR_SQL2).execute().getNodes();
                JCRNodeWrapper fileNode;
                PropertyIterator ref;
                while (it.hasNext()) {
                    fileNode = (JCRNodeWrapper) it.nextNode();
                    ref = fileNode.getReferences();
                    if (ref != null && ref.getSize() == 0) {
                        logger.debug("File unreferenced: {}", fileNode.getPath());
                        nbFilesUnreferenced++;
                    } else {
                        ref = fileNode.getWeakReferences();
                        if (ref != null && ref.getSize() == 0) {
                            logger.debug("File unreferenced: {}", fileNode.getPath());
                            nbFilesUnreferenced++;
                        }
                    }
                }
                return nbFilesUnreferenced;
            });
        } catch (RepositoryException e) {
            logger.error("", e);
        }
        return new ProbeStatus(String.format("%d files unreferenced", nbFiles), ProbeStatus.Health.GREEN);
    }
}
