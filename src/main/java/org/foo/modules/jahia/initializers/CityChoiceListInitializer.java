package org.foo.modules.jahia.initializers;

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
public class CityChoiceListInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(CityChoiceListInitializer.class);

    @Override
    public void setKey(String key) {
        // Nothing to do
    }

    @Override
    public String getKey() {
        return "cityChoiceListInitializer";
    }

    private static final Map<String, List<ChoiceListValue>> cities = new HashMap<>();

    static {
        cities.put("ile-de-france", Arrays.asList(new ChoiceListValue("paris", "paris"), new ChoiceListValue("nanterre", "nanterre"), new ChoiceListValue("villejuif", "villejuif")));
        cities.put("bretagne", Arrays.asList(new ChoiceListValue("renne", "renne"), new ChoiceListValue("lancieux", "lancieux"), new ChoiceListValue("dinard", "dinard")));
        cities.put("alsace", Arrays.asList(new ChoiceListValue("strasbourg", "strasbourg"), new ChoiceListValue("epinal", "epinal"), new ChoiceListValue("nancy", "nancy")));

        cities.put("dorset", Arrays.asList(new ChoiceListValue("weymouth", "weymouth"), new ChoiceListValue("bournemouth", "bournemouth"), new ChoiceListValue("bridport", "bridport")));
        cities.put("gloucestershire", Arrays.asList(new ChoiceListValue("gloucester", "gloucester"), new ChoiceListValue("stroud", "stroud"), new ChoiceListValue("bibury", "bibury")));
        cities.put("hampshire", Arrays.asList(new ChoiceListValue("winchester", "winchester"), new ChoiceListValue("portsmouth", "portsmouth"), new ChoiceListValue("southampton", "southampton")));

        cities.put("hessen", Arrays.asList(new ChoiceListValue("giessen", "giessen"), new ChoiceListValue("marbourg", "marbourg"), new ChoiceListValue("darmstadt", "darmstadt")));
        cities.put("bavaria", Arrays.asList(new ChoiceListValue("nuremberg", "nuremberg"), new ChoiceListValue("munich", "munich"), new ChoiceListValue("augsbourg", "augsbourg")));
        cities.put("saxony", Arrays.asList(new ChoiceListValue("leipzig", "leipzig"), new ChoiceListValue("dresde", "dresde"), new ChoiceListValue("freiberg", "freiberg")));
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition extendedPropertyDefinition, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<String> regions = ChoiceListHelper.getProperty("region", context);
        List<ChoiceListValue> choiceListValues = new ArrayList<>();
        if (regions != null && !regions.isEmpty()) {
            for (String region : regions) {
                if (cities.containsKey(region)) {
                    choiceListValues.addAll(cities.get(region));
                }
            }
        }
        return choiceListValues;
    }
}
