package org.foo.modules.jahia.initializers;

import org.foo.modules.jahia.helpers.ChoiceListHelper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component(service = ModuleChoiceListInitializer.class)
public class RegionChoiceListInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(RegionChoiceListInitializer.class);

    @Override
    public void setKey(String key) {
        // Nothing to do
    }

    @Override
    public String getKey() {
        return "regionChoiceListInitializer";
    }

    private static final Map<String, List<ChoiceListValue>> regions = new HashMap<>();

    static {
        regions.put("france", Arrays.asList(new ChoiceListValue("ile-de-france", "ile-de-france"), new ChoiceListValue("bretagne", "bretagne"), new ChoiceListValue("alsace", "alsace")));
        regions.put("england", Arrays.asList(new ChoiceListValue("dorset", "dorset"), new ChoiceListValue("gloucestershire", "gloucestershire"), new ChoiceListValue("hampshire", "hampshire")));
        regions.put("deutschland", Arrays.asList(new ChoiceListValue("hessen", "hessen"), new ChoiceListValue("bavaria", "bavaria"), new ChoiceListValue("saxony", "saxony")));
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition extendedPropertyDefinition, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<String> countries = ChoiceListHelper.getProperty("country", context);
        List<ChoiceListValue> choiceListValues = new ArrayList<>();
        if (countries != null && !countries.isEmpty()) {
            for (String country : countries) {
                if (regions.containsKey(country)) {
                    choiceListValues.addAll(regions.get(country));
                }
            }
        }
        return choiceListValues;
    }
}
