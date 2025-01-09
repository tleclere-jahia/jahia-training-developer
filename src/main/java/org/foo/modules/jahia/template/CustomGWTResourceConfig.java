package org.foo.modules.jahia.template;

import org.jahia.ajax.gwt.utils.GWTResourceConfig;
import org.jahia.services.SpringContextSingleton;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.List;

@Component(service = CustomGWTResourceConfig.class, immediate = true)
public class CustomGWTResourceConfig {

    private static final String STYLE = "/modules/jahia-training-developer/css/template.css";

    @Activate
    private void onActivate() {
        List<String> styles = ((GWTResourceConfig) SpringContextSingleton.getBean("GWTResourceConfig")).getCssStyles();
        if (!styles.contains(STYLE)) {
            styles.add(STYLE);
        }
    }

    @Deactivate
    private void onDeactivate() {
        (((GWTResourceConfig) SpringContextSingleton.getBean("GWTResourceConfig")).getCssStyles()).remove(STYLE);
    }
}
