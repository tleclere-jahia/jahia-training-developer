package org.foo.modules.jahia.initializers;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.jahia.services.notification.HttpClientService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component(service = ModuleChoiceListInitializer.class, configurationPid = "org.foo.modules.jahiatrainingdeveloper.picto")
@Designate(ocd = PictoInitializer.Config.class)
public class PictoInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(PictoInitializer.class);

    @ObjectClassDefinition(name = "%jahiatrainingdeveloper.config", description = "%jahiatrainingdeveloper.description", localization = "OSGI-INF/l10n/jahiatrainingdeveloper")
    public @interface Config {
        @AttributeDefinition(name = "%jahiatrainingdeveloper.pictoUrl", description = "%jahiatrainingdeveloper.pictoUrl.description")
        String pictoUrl();
    }

    private String url;

    @Reference
    private HttpClientService httpClientService;

    @Activate
    @Modified
    public void onActivate(Config config) {
        url = config.pictoUrl();
    }

    @Override
    public void setKey(String key) {
        // Nothing to do
    }

    @Override
    public String getKey() {
        return "picto";
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition declaringPropertyDefinition, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        if (url != null) {
            String svgs = httpClientService.executeGet(url);
            if (svgs != null) {
                values = new ArrayList<>();
                JSONArray items = new JSONObject(svgs).getJSONArray("Actions");
                JSONObject item;
                for (int i = 0; i < items.length(); i++) {
                    item = items.getJSONObject(i);
                    values.add(new ChoiceListValue(item.optString("name").concat(" - ").concat(item.optString("keywords")),
                            Collections.singletonMap("iconStart", item.optString("svg").replaceAll("style", "styles")),
                            new ValueImpl("icon ".concat(item.optString("class")))));
                }
            }
        }
        return values;
    }
}
