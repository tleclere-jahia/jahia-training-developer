package org.foo.modules.jahia.validators;

import org.jahia.api.Constants;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueTitleValidator.class)
public @interface UniqueTitle {
    Class<?>[] groups() default {};

    String message() default "{javax.validation.constraints.unique.title}";

    Class<? extends Payload>[] payload() default {};

    String title() default Constants.JCR_TITLE;
}
