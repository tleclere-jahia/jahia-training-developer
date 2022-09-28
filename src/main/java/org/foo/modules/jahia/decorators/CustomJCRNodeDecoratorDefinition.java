package org.foo.modules.jahia.decorators;

import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.content.decorator.JCRNodeDecoratorDefinition;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;

@Component(service = JCRNodeDecoratorDefinition.class)
public class CustomJCRNodeDecoratorDefinition extends JCRNodeDecoratorDefinition {
    private final Map<String, Class<? extends JCRNodeDecorator>> savedDecorators;
    private JCRStoreService jcrStoreService;

    public CustomJCRNodeDecoratorDefinition() {
        savedDecorators = new HashMap<>();
    }

    @Reference
    private void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    @Activate
    private void onActivate(BundleContext bundleContext) {
        // save previous decorators
        jcrStoreService.getDecorators().entrySet().stream()
                .filter(entry -> "jnt:user".equals(entry.getKey()) || "jnt:externalUser".equals(entry.getKey()))
                .forEach(entry -> savedDecorators.put(entry.getKey(), entry.getValue()));

        // add or override decorators
        setJahiaModule(BundleUtils.getModule(bundleContext.getBundle()));
        Map<String, String> decorators = new HashMap<>();
        decorators.put("jnt:user", JCRUserNodeDecorator.class.getName());
        decorators.put("jnt:externalUser", JCRExternalUserNodeDecorator.class.getName());
        setDecorators(decorators);
    }

    @Deactivate
    private void onDeactivate() {
        Map<String, Class> decorators = getDecorators();
        if (decorators != null) {
            for (Map.Entry<String, Class> decorator : decorators.entrySet()) {
                jcrStoreService.removeDecorator(decorator.getKey());
            }
        }

        // restore previous decorators
        savedDecorators.forEach((key, value) -> jcrStoreService.addDecorator(key, value));
    }
}
