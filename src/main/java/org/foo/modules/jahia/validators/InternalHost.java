package org.foo.modules.jahia.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InternalHostValidator.class)
public @interface InternalHost {
    Class<?>[] groups() default {};

    String message() default "{javax.validation.constraints.internalhostinvalid}";

    Class<? extends Payload>[] payload() default {};
}
