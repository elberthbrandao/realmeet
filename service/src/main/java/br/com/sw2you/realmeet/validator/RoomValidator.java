package br.com.sw2you.realmeet.validator;

import static br.com.sw2you.realmeet.validator.ValidatorConstants.*;
import static br.com.sw2you.realmeet.validator.ValidatorUtils.*;

import br.com.sw2you.realmeet.api.model.CreateRoomDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.ValidationUtils;

@Component
public class RoomValidator {

    public void validate(CreateRoomDTO createRoomDTO) {
        var validationErrors = new ValidationErrors();

        //Room Name
        validateRequired(createRoomDTO.getName(), ROOM_NAME, validationErrors);
        validateMaxLength(createRoomDTO.getName(), ROOM_NAME, ROOM_NAME_MAX_LENGTH, validationErrors);

        throwOnError(validationErrors);
    }
}
