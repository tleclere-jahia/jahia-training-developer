package org.foo.modules.jahia.initializers;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component(service = ModuleChoiceListInitializer.class)
public class ExcludeMeInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ExcludeMeInitializer.class);

    @Override
    public void setKey(String s) {
        // Do nothing
    }

    @Override
    public String getKey() {
        return "excludeMe";
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition extendedPropertyDefinition, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        JCRNodeWrapper node = Optional.of(context).map(ctx -> (JCRNodeWrapper) Optional.ofNullable(context.get("contextNode")).orElse(context.get("contextParent"))).orElse(null);
        if (node == null) {
            return values;
        }
        try {
            String nodeIdentifier = node.getIdentifier();
            Iterator<ChoiceListValue> it = values.iterator();
            while(it.hasNext()) {
                if(nodeIdentifier.equals(it.next().getValue().getString())) {
                    it.remove();
                }
            }
            return values;
        } catch (RepositoryException e) {
            logger.error("", e);
        }
        return null;
    }
}
