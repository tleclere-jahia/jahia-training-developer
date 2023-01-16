package org.foo.modules.jahia.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.jcr.RepositoryException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;

public class TextValidator implements ConstraintValidator<HtmlValid, FoomixTextValidator> {
    private static final Logger logger = LoggerFactory.getLogger(TextValidator.class);

    private String errorMessage;

    @Override
    public void initialize(HtmlValid htmlValid) {
        errorMessage = htmlValid.message();
    }

    @Override
    public boolean isValid(FoomixTextValidator foomixTextValidator, ConstraintValidatorContext constraintValidatorContext) {
        try {
            return foomixTextValidator.getPropertiesToValidate().entrySet().stream().allMatch(entry -> {
                try {
                    SAXParserFactory.newInstance().newSAXParser().getXMLReader().parse(new InputSource(new StringReader("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>" + entry.getValue().getString())));
                    return true;
                } catch (Exception e) {
                    // not valid XML String
                    constraintValidatorContext.buildConstraintViolationWithTemplate(errorMessage)
                            .addPropertyNode(entry.getKey()).addConstraintViolation();
                    return false;
                }
            });
        } catch (RepositoryException e) {
            logger.error("", e);
            return false;
        }
    }
}
