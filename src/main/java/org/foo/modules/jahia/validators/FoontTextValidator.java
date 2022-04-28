package org.foo.modules.jahia.validators;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.validation.JCRNodeValidator;

public class FoontTextValidator implements JCRNodeValidator {
    private final JCRNodeWrapper jcrNodeWrapper;

    public FoontTextValidator(JCRNodeWrapper jcrNodeWrapper) {
        this.jcrNodeWrapper = jcrNodeWrapper;
    }

    @HtmlValid
    public String getText() {
        return jcrNodeWrapper.getPropertyAsString("text");
    }
}
