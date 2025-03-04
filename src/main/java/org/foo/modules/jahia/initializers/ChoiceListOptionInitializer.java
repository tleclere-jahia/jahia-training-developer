package org.foo.modules.jahia.initializers;

import org.jahia.api.Constants;
import org.jahia.api.content.JCRTemplate;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.NodesChoiceListInitializerImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component(service = ModuleChoiceListInitializer.class)
public class ChoiceListOptionInitializer extends NodesChoiceListInitializerImpl implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ChoiceListOptionInitializer.class);

    @Reference
    private JCRTemplate jcrTemplate;

    @Override
    public void setKey(String key) {
        // Nothing to do
    }

    @Override
    public String getKey() {
        return "choiceListOption";
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<ChoiceListValue> choiceListValues = super.getChoiceListValues(epd, param, values, locale, context);

        try {
            jcrTemplate.doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, null, systemSession -> {
                choiceListValues.forEach(v -> {
                    try {
                        if (systemSession.getNodeByIdentifier(v.getValue().getString()).getProperty("isDefaultValue").getBoolean()) {
                            v.addProperty("defaultProperty", true);
                        }
                    } catch (RepositoryException e) {
                        logger.error("", e);
                    }
                });
                return null;
            });
        } catch (RepositoryException e) {
            logger.error("", e);
        }

        return choiceListValues;
    }
}
