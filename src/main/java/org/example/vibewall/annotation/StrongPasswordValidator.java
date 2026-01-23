package org.example.vibewall.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    @Override
    public boolean isValid(String password,
                           ConstraintValidatorContext context) {

        if (password == null || password.isBlank()) {
            return false;
        }

        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
