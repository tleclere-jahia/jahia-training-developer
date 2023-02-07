package org.foo.modules.jahia.servlets;

import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.bin.filters.ServletFilter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component(service = AbstractServletFilter.class)
public class OrbeonServletFilter extends ServletFilter {

    @Activate
    private void onActivate(Map<String, String> configuration) {
        setFilter(new org.orbeon.oxf.fr.embedding.servlet.ServletFilter());
        setFilterName("orbeon-form-runner-filter");
        Map<String, String> initParams = new HashMap<>();
        initParams.put("form-runner-url", configuration.get("form-runner-url"));
        initParams.put("orbeon-prefix", "/orbeon");
        setParameters(initParams);
        Set<String> dispatcherTypes = new HashSet<>();
        dispatcherTypes.add("REQUEST");
        dispatcherTypes.add("FORWARD");
        setDispatcherTypes(dispatcherTypes);
        setUrlPatterns(new String[]{"*.html", "/orbeon/*"});
    }
}
