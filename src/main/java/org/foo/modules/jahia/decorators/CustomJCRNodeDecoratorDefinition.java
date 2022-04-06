package org.foo.modules.jahia.decorators;

import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.decorator.JCRNodeDecoratorDefinition;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;

@Component(service = JCRNodeDecoratorDefinition.class, immediate = true)
public class CustomJCRNodeDecoratorDefinition extends JCRNodeDecoratorDefinition {
    public CustomJCRNodeDecoratorDefinition() {
        setJahiaModule(BundleUtils.getModule(FrameworkUtil.getBundle(JCRUserNodeDecorator.class)));
        Map<String, String> decorators = new HashMap<>();
        decorators.put("jnt:user", JCRUserNodeDecorator.class.getName());
        setDecorators(decorators);
    }
}
