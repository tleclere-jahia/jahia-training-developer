package org.foo.modules.jahia.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TextValidator.class)
@Documented
public @interface HtmlValid {
    Class<?>[] groups() default {};

    String message() default "{javax.validation.constraints.htmlinvalid}";

    Class<? extends Payload>[] payload() default {};
}
