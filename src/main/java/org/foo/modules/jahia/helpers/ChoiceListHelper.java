package org.foo.modules.jahia.helpers;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ChoiceListHelper {
    private ChoiceListHelper() {
        // No constructor
    }

    public static List<String> getProperty(String propertyName, Map<String, Object> context) {
        // Read the property on the node if not in the context
        if (context.containsKey(propertyName)) {
            return (List<String>) context.get(propertyName);
        }

        List<String> country = new ArrayList<>();
        try {
            if (context.get("contextNode") != null) {
                final JCRNodeWrapper contextNode = (JCRNodeWrapper) context.get("contextNode");
                if (contextNode.hasProperty(propertyName)) {
                    String value = contextNode.getPropertyAsString(propertyName);
                    if (contextNode.getProperty(propertyName).isMultiple()) {
                        country.addAll(Arrays.asList(StringUtils.split(value, " ")));
                    } else {
                        country.add(value);
                    }
                }
            }
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return country;
    }
}
