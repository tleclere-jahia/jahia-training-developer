package org.foo.modules.jahia.validators;

import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.decorator.validation.JCRNodeValidatorDefinition;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;

@Component(service = JCRNodeValidatorDefinition.class)
public class CustomJCRNodeValidatorDefinition extends JCRNodeValidatorDefinition {
    @Activate
    private void onActivate(BundleContext bundleContext) {
        setJahiaModule(BundleUtils.getModule(bundleContext.getBundle()));
        setValidators(Collections.singletonMap("foomix:richText", FoomixTextValidator.class.getName()));
    }
}
