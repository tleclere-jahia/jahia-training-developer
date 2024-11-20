package org.foo.modules.jahia.initializers;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component(service = ModuleChoiceListInitializer.class)
public class AddMixinInitializer implements ModuleChoiceListInitializer {
    @Override
    public void setKey(String s) {
        // Do nothing
    }

    @Override
    public String getKey() {
        return "addMixinInitializer";
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition extendedPropertyDefinition, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        return Arrays.stream(extendedPropertyDefinition.getValueConstraints())
                .map(mixin -> new ChoiceListValue(mixin, Collections.singletonMap("addMixin", mixin), new ValueImpl(mixin)))
                .collect(Collectors.toList());
    }
}
