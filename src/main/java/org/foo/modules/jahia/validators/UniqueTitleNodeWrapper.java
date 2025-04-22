package org.foo.modules.jahia.validators;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.validation.JCRNodeValidator;

@UniqueTitle
public class UniqueTitleNodeWrapper implements JCRNodeValidator {
    private final JCRNodeWrapper jcrNodeWrapper;

    public UniqueTitleNodeWrapper(JCRNodeWrapper jcrNodeWrapper) {
        this.jcrNodeWrapper = jcrNodeWrapper;
    }

    public JCRNodeWrapper getJcrNodeWrapper() {
        return jcrNodeWrapper;
    }
}
