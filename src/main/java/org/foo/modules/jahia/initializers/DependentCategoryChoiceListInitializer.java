package org.foo.modules.jahia.initializers;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component(service = ModuleChoiceListInitializer.class)
public class DependentCategoryChoiceListInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DependentCategoryChoiceListInitializer.class);

    @Override
    public void setKey(String key) {
        // Do nothing
    }

    @Override
    public String getKey() {
        return "dependentCategory";
    }

    private JCRSessionFactory jcrSessionFactory;

    @Reference
    private void setJcrSessionFactory(JCRSessionFactory jcrSessionFactory) {
        this.jcrSessionFactory = jcrSessionFactory;
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition extendedPropertyDefinition, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<String> categories = ChoiceListHelper.getProperty("category", context);
        if (categories != null && !categories.isEmpty()) {
            for (String categoryIdentifier : categories) {
                try {
                    JCRNodeWrapper choosenNode = jcrSessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, locale).getNodeByIdentifier(categoryIdentifier);
                    return JCRContentUtils.getChildrenOfType(choosenNode, "jnt:category").stream()
                            .map(child -> {
                                try {
                                    return new ChoiceListValue(child.getPropertyAsString(Constants.JCR_TITLE), child.getIdentifier());
                                } catch (RepositoryException e) {
                                    logger.error("", e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                } catch (RepositoryException e) {
                    logger.warn("Unable to load subcategories", e);
                }
            }
        }
        return Collections.emptyList();
    }
}
