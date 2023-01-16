package org.foo.modules.jahia.validators;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.decorator.validation.JCRNodeValidator;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.SelectorType;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HtmlValid
@InternalHost
public class FoomixTextValidator implements JCRNodeValidator {
    private final JCRNodeWrapper jcrNodeWrapper;

    public FoomixTextValidator(JCRNodeWrapper jcrNodeWrapper) {
        this.jcrNodeWrapper = jcrNodeWrapper;
    }

    public Map<String, JCRPropertyWrapper> getPropertiesToValidate() throws RepositoryException {
        Map<String, JCRPropertyWrapper> propertiesToValidate = new HashMap<>();

        ExtendedNodeType[] mixinNodeTypes = jcrNodeWrapper.getMixinNodeTypes();
        final List<ExtendedNodeType> superTypes = new ArrayList<>(mixinNodeTypes.length + 1);
        superTypes.add(jcrNodeWrapper.getPrimaryNodeType());
        superTypes.addAll(Arrays.asList(mixinNodeTypes));

        for (ExtendedNodeType superType : superTypes) {
            ExtendedPropertyDefinition[] propertyDefinitions = superType.getPropertyDefinitions();
            int i = 0, nbDefinitions = propertyDefinitions.length;
            ExtendedPropertyDefinition propertyDefinition;
            for (; i < nbDefinitions; i++) {
                propertyDefinition = propertyDefinitions[i];
                if (propertyDefinition.getRequiredType() == PropertyType.STRING
                        && propertyDefinition.getSelector() == SelectorType.RICHTEXT
                        && jcrNodeWrapper.hasProperty(propertyDefinition.getName())) {
                    propertiesToValidate.put(propertyDefinition.getName(), jcrNodeWrapper.getProperty(propertyDefinition.getName()));
                }
            }
        }
        return propertiesToValidate;
    }
}
