package br.com.sw2you.realmeet.validator;

import br.com.sw2you.realmeet.exception.InvalidRequestException;

public final class ValidatorUtils {

    private ValidatorUtils() {}

    public static void throwOnError(ValidationErrors validationErrors) {
        if(validationErrors.hasErrors()) {
            throw new InvalidRequestException(validationErrors);
        }
    }
}
