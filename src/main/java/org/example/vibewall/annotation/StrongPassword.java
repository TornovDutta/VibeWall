package org.example.vibewall.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default
            "Password must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
