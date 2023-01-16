package org.foo.modules.jahia.validators;

import org.apache.commons.lang.StringUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class InternalHostValidator implements ConstraintValidator<InternalHost, FoomixTextValidator> {
    private static final Logger logger = LoggerFactory.getLogger(InternalHostValidator.class);

    private String errorMessage;

    @Override
    public void initialize(InternalHost internalHostValidation) {
        errorMessage = internalHostValidation.message();
    }

    @Override
    public boolean isValid(FoomixTextValidator foomixTextValidator, ConstraintValidatorContext constraintValidatorContext) {
        try {
            return foomixTextValidator.getPropertiesToValidate().entrySet().stream().allMatch(entry -> {
                boolean result = false;
                try {
                    result = valideHost(entry.getValue().getString());
                } catch (RepositoryException e) {
                    // Repository exception
                }
                if (!result) {
                    constraintValidatorContext.buildConstraintViolationWithTemplate(errorMessage)
                            .addPropertyNode(entry.getKey()).addConstraintViolation();
                }
                return result;
            });
        } catch (RepositoryException e) {
            logger.error("", e);
            return false;
        }
    }

    private boolean valideHost(String value) {
        try {
            if (value == null) {
                return false;
            }
            URI uri = new URI(value);
            return BundleUtils.getOsgiService(JahiaSitesService.class, null).getSitesNodeList().stream()
                    .map(JCRSiteNode::getAllServerNames)
                    .flatMap(Collection::stream)
                    .noneMatch(serverName -> StringUtils.equals(uri.getHost(), serverName));
        } catch (URISyntaxException | RepositoryException e) {
            logger.error("", e);
            return false;
        }
    }
}
