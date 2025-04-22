package org.foo.modules.jahia.validators;

import org.jahia.services.content.decorator.validation.JCRNodeValidator;
import org.jahia.services.content.decorator.validation.JCRNodeValidatorDefinition;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component(service = JCRNodeValidatorDefinition.class)
public class CustomJCRNodeValidatorDefinition extends JCRNodeValidatorDefinition {
    @Override
    public Map<String, Class> getValidators() {
        Map<String, Class<? extends JCRNodeValidator>> validators = new HashMap<>();
        validators.put("foomix:richText", FoomixTextValidator.class);
        validators.put("foont:uniqueTitle", UniqueTitleNodeWrapper.class);
        return Collections.unmodifiableMap(validators);
    }
}
