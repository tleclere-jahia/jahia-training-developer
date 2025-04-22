package org.foo.modules.jahia.validators;

import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueTitleValidator implements ConstraintValidator<UniqueTitle, UniqueTitleNodeWrapper> {
    private static final Logger logger = LoggerFactory.getLogger(UniqueTitleValidator.class);

    private String title;
    private String errorMessage;

    @Override
    public void initialize(UniqueTitle annotation) {
        title = annotation.title();
        errorMessage = annotation.message();
    }

    @Override
    public boolean isValid(UniqueTitleNodeWrapper validator, ConstraintValidatorContext constraintValidatorContext) {
        try {
            JCRNodeWrapper node = validator.getJcrNodeWrapper();
            String nodeIdentifier = node.getIdentifier();
            JCRNodeIteratorWrapper it = node.getParent().getNodes();
            boolean titleExists = false;
            while (!titleExists && it.hasNext()) {
                node = (JCRNodeWrapper) it.nextNode();
                if (!nodeIdentifier.equals(node.getIdentifier())) {
                    titleExists = validator.getJcrNodeWrapper().getPropertyAsString(title).equals(node.getPropertyAsString(title));
                }
            }
            if (titleExists) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode(title).addConstraintViolation();
            }
            return !titleExists;
        } catch (RepositoryException e) {
            logger.error("", e);
            return false;
        }
    }
}
