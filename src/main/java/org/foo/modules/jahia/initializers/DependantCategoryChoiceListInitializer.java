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
public class DependantCategoryChoiceListInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DependantCategoryChoiceListInitializer.class);
    public static final String KEY = "dependantCategory";

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
        if (StringUtils.isBlank(propertyName) || !context.containsKey(propertyName) || CollectionUtils.isEmpty((List<String>) context.get(propertyName))) {
            return Collections.emptyList();
        }

        if (StringUtils.isNotEmpty(propertyName) && context.containsKey(propertyName)) {
            JCRNodeWrapper node = Optional.of(context).map(ctx -> (JCRNodeWrapper) Optional.ofNullable(context.get("contextParent")).orElse(context.get("contextNode"))).orElse(null);
            if (node != null) {
                try {
                    String categoryUUID = ((List<String>) context.get(propertyName)).get(0);
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
        }
        return result;
    }

}