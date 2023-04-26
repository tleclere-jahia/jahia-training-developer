package org.foo.modules.jahia.initializers;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component(service = ModuleChoiceListInitializer.class)
public class CardTypeInitializer implements ModuleChoiceListInitializer {
    @Override
    public void setKey(String key) {
        // Nothing to do
    }

    @Override
    public String getKey() {
        return "cardTypeInitializer";
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<ChoiceListValue> choiceListValues = new ArrayList<>();
        choiceListValues.add(new ChoiceListValue("default", Collections.singletonMap("addMixin", "foomix:cardDefault"), new ValueImpl("default")));
        choiceListValues.add(new ChoiceListValue("type1", Collections.singletonMap("addMixin", "foomix:cardType1"), new ValueImpl("type1")));
        choiceListValues.add(new ChoiceListValue("type2", Collections.singletonMap("addMixin", "foomix:cardType2"), new ValueImpl("type2")));
        return choiceListValues;
    }
}
