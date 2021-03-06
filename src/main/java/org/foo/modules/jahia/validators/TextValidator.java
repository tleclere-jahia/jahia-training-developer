package org.foo.modules.jahia.validators;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.SelectorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TextValidator implements ConstraintValidator<HtmlValid, FoomixTextValidator> {
    private static final Logger logger = LoggerFactory.getLogger(TextValidator.class);

    private String errorMessage;

    @Override
    public void initialize(HtmlValid htmlValid) {
        // Nothing to do
        errorMessage = htmlValid.message();
    }

    @Override
    public boolean isValid(FoomixTextValidator foomixTextValidator, ConstraintValidatorContext constraintValidatorContext) {
        JCRNodeWrapper jcrNodeWrapper = foomixTextValidator.getJcrNodeWrapper();
        boolean result = true;
        try {
            ExtendedNodeType[] mixinNodeTypes = jcrNodeWrapper.getMixinNodeTypes();
            final List<ExtendedNodeType> superTypes = new ArrayList<>(mixinNodeTypes.length + 1);
            superTypes.add(jcrNodeWrapper.getPrimaryNodeType());
            superTypes.addAll(Arrays.asList(mixinNodeTypes));

            Iterator<ExtendedNodeType> it = superTypes.iterator();
            while (it.hasNext() && result) {
                result = checkProperties(jcrNodeWrapper, it.next(), constraintValidatorContext);
            }
        } catch (RepositoryException e) {
            logger.error("", e);
        }
        return result;
    }

    private boolean checkProperties(JCRNodeWrapper jcrNodeWrapper, ExtendedNodeType extendedNodeType, ConstraintValidatorContext constraintValidatorContext) throws RepositoryException {
        boolean result = true;
        ExtendedPropertyDefinition[] propertyDefinitions = extendedNodeType.getPropertyDefinitions();
        int i = 0, nbDefinitions = propertyDefinitions.length;
        ExtendedPropertyDefinition propertyDefinition;
        for (; i < nbDefinitions && result; i++) {
            propertyDefinition = propertyDefinitions[i];
            if (propertyDefinition.getRequiredType() == PropertyType.STRING
                    && propertyDefinition.getSelector() == SelectorType.RICHTEXT
                    && jcrNodeWrapper.hasProperty(propertyDefinition.getName())) {
                result = validate(jcrNodeWrapper.getPropertyAsString(propertyDefinition.getName()));
                if (!result) {
                    constraintValidatorContext.buildConstraintViolationWithTemplate(errorMessage)
                            .addPropertyNode(propertyDefinition.getName()).addConstraintViolation();
                }
            }
        }
        return result;
    }

    private boolean validate(String text) {
        try {
            HttpResponse<JsonNode> uniResponse = Unirest.post("https://validator.w3.org/check")
                    .field("fragment", text)
                    .field("doctype", "Inline")
                    .field("prefill", 1)
                    .field("prefill_doctype", "html5")
                    .field("group", 0)
                    .field("output", "json")
                    .asJson();
            return uniResponse.getBody().getObject().getJSONArray("messages").length() == 0;
        } catch (UnirestException e) {
            logger.error("", e);
        }
        return false;
    }
}
