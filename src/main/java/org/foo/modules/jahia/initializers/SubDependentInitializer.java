package org.foo.modules.jahia.initializers;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component(service = ModuleChoiceListInitializer.class)
public class SubDependentInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(SubDependentInitializer.class);

    @Override
    public void setKey(String key) {
        // Nothing to do
    }

    @Override
    public String getKey() {
        return "subdependent";
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        String dependentProperties = context.containsKey("dependentProperties") ? ((List<String>) context.get("dependentProperties")).get(0) : null;
        if (StringUtils.isBlank(dependentProperties)) {
            return Collections.emptyList();
        }

        Optional.of(context).map(ctx -> (JCRNodeWrapper) Optional.ofNullable(context.get("contextNode")).orElse(context.get("contextParent")))
                .ifPresent(node -> Arrays.stream(dependentProperties.split(",")).forEach(propertyName -> {
                    String value = null;
                    try {
                        if (!context.containsKey(propertyName) || CollectionUtils.isEmpty((List<String>) context.get(propertyName))) {
                            if (node.hasProperty(propertyName)) {
                                value = node.getPropertyAsString(propertyName);
                            }
                        } else {
                            value = ((List<String>) context.get(propertyName)).get(0);
                        }
                    } catch (RepositoryException e) {
                        logger.warn("Unable to load subcategories", e);
                    }
                    logger.debug("{}: {}", propertyName, value);
                }));
        return Collections.emptyList();
    }
}
