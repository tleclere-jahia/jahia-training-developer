package org.foo.modules.jahia.framework;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.test.ModuleTestHelper;
import org.jahia.test.framework.DeployResourcesTestExecutionListener;
import org.jahia.utils.PomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class DeployAllResourcesTestExecutionListener extends DeployResourcesTestExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(DeployAllResourcesTestExecutionListener.class);

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        Set<String> artifacts = new HashSet<>();
        Model pom = PomUtils.read(new File("pom.xml"));
        for (Dependency dependency : pom.getDependencies()) {
            if (dependency.getVersion() != null && dependency.getVersion().matches("^[1-9].*$")) {
                File module = ModuleTestHelper.getModuleFromMaven(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
                if (module != null) {
                    try (JarFile jarFile = new JarFile(module)) {
                        ZipEntry zipEntry = jarFile.getEntry("META-INF/definitions.cnd");
                        if (zipEntry != null) {
                            try (InputStream fs = jarFile.getInputStream(zipEntry)) {
                                File cndFile = File.createTempFile("definitions", "cnd");
                                logger.info("Deploy definitions from module {}", dependency.getArtifactId());
                                FileUtils.copyInputStreamToFile(fs, cndFile);
                                NodeTypeRegistry.getInstance().addDefinitionsFile(cndFile, dependency.getArtifactId());
                                artifacts.add(dependency.getArtifactId());
                                FileUtils.deleteQuietly(cndFile);
                            }
                        }
                    }
                }
            }
        }

        artifacts.forEach(artifact -> {
            try {
                if (NodeTypeRegistry.getInstance().getFiles(artifact) != null) {
                    JCRStoreService.getInstance().deployDefinitions(artifact, null, -1);
                }
            } catch (IOException | RepositoryException e) {
                logger.error("", e);
            }
        });

        super.prepareTestInstance(testContext);
    }
}
