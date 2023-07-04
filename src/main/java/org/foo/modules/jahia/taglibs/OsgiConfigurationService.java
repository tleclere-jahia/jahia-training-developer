package org.foo.modules.jahia.taglibs;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component(service = OsgiConfigurationService.class)
public class OsgiConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(OsgiConfigurationService.class);

    private ConfigurationAdmin configurationAdmin;

    @Reference
    private void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    public List<Object> getPropertyFromConfiguration(String factoryPid, String pid, String property) {
        try {
            Configuration[] configurations;
            if (factoryPid != null) {
                configurations = configurationAdmin.listConfigurations("(service.factoryPid=" + factoryPid + ")");
            } else if (pid != null) {
                configurations = configurationAdmin.listConfigurations("(service.pid=" + pid + ")");
            } else {
                configurations = configurationAdmin.listConfigurations(null);
            }
            if (configurations == null) {
                return null;
            }

            return Arrays.stream(configurations)
                    .filter(configuration -> configuration.getProperties() != null
                            && configuration.getProperties().get(property) != null)
                    .map(config -> config.getProperties().get(property))
                    .collect(Collectors.toList());
        } catch (IOException | InvalidSyntaxException e) {
            logger.error("", e);
        }
        return null;
    }
}
