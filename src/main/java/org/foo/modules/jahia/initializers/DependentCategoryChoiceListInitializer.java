package org.foo.modules.jahia.initializers;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.*;

@Component(service = ModuleChoiceListInitializer.class, immediate = true)
public class DependentCategoryChoiceListInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DependentCategoryChoiceListInitializer.class);
    private static final String KEY = "dependentCategory";

    @Override
    public void setKey(String key) {
        // Do nothing
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<ChoiceListValue> result = new ArrayList<>();
        String propertyName = context.containsKey("dependentProperties") ? ((List<String>) context.get("dependentProperties")).get(0) : null;
        if (StringUtils.isBlank(propertyName)) {
            return Collections.emptyList();
        }

        JCRNodeWrapper node = Optional.of(context).map(ctx -> (JCRNodeWrapper) Optional.ofNullable(context.get("contextParent")).orElse(context.get("contextNode"))).orElse(null);
        if (node != null) {
            try {
                String categoryUUID;
                if (!context.containsKey(propertyName) || CollectionUtils.isEmpty((List<String>) context.get(propertyName))) {
                    if (!node.hasProperty(propertyName)) {
                        return Collections.emptyList();
                    }
                    categoryUUID = node.getProperty(propertyName).getNode().getIdentifier();
                } else {
                    categoryUUID = ((List<String>) context.get(propertyName)).get(0);
                }
                if (StringUtils.isBlank(categoryUUID)) {
                    return Collections.emptyList();
                }
                JCRNodeWrapper choosenNode = node.getSession().getNodeByIdentifier(categoryUUID);
                JCRNodeIteratorWrapper nodesIterator = choosenNode.getNodes();
                JCRNodeWrapper child;
                ChoiceListValue val;
                while (nodesIterator.hasNext()) {
                    child = (JCRNodeWrapper) nodesIterator.nextNode();
                    val = new ChoiceListValue(child.getPropertyAsString(Constants.JCR_TITLE), child.getIdentifier());
                    result.add(val);
                }
            } catch (RepositoryException e) {
                logger.warn("Unable to load subcategories", e);
            }
        }
        return result;
    }

}
