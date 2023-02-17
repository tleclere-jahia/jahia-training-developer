package org.foo.modules.jahia.validators;

import org.apache.commons.lang.StringUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser;
import org.jahia.services.sites.JahiaSitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InternalHostValidator implements ConstraintValidator<InternalHost, FoomixTextValidator> {
    private static final Logger logger = LoggerFactory.getLogger(InternalHostValidator.class);

    private String errorMessage;
    private final HtmlTagAttributeTraverser htmlTagAttributeTraverser;

    public InternalHostValidator() {
        Map<String, Set<String>> attributesToVisit = new HashMap<>();
        attributesToVisit.put("a", Collections.singleton("href"));
        attributesToVisit.put("img", Collections.singleton("src"));
        htmlTagAttributeTraverser = new HtmlTagAttributeTraverser(attributesToVisit);
    }

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

            return htmlTagAttributeTraverser.traverse(value, (String v, RenderContext context, String tagName, String attrName, Resource resource) -> {
                try {
                    URI uri = new URI(v);
                    if (BundleUtils.getOsgiService(JahiaSitesService.class, null).getSitesNodeList().stream()
                            .map(JCRSiteNode::getAllServerNames)
                            .flatMap(Collection::stream)
                            .noneMatch(serverName -> StringUtils.equals(uri.getHost(), serverName))) {
                        return v;
                    }
                } catch (URISyntaxException | RepositoryException e) {
                    logger.error("", e);
                }
                return null;
            }) != null;
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }
}
