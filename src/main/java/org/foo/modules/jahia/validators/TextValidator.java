package org.foo.modules.jahia.validators;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TextValidator implements ConstraintValidator<HtmlValid, String> {
    private static final Logger logger = LoggerFactory.getLogger(TextValidator.class);

    @Override
    public void initialize(HtmlValid htmlValid) {
        // Nothing to do
    }

    @Override
    public boolean isValid(String text, ConstraintValidatorContext constraintValidatorContext) {
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
