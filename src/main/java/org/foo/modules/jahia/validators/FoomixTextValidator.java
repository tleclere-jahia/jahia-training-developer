package org.foo.modules.jahia.validators;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.validation.JCRNodeValidator;

@HtmlValid
public class FoomixTextValidator implements JCRNodeValidator {
    private final JCRNodeWrapper jcrNodeWrapper;

    public FoomixTextValidator(JCRNodeWrapper jcrNodeWrapper) {
        this.jcrNodeWrapper = jcrNodeWrapper;
    }

    public JCRNodeWrapper getJcrNodeWrapper() {
        return jcrNodeWrapper;
    }
}
