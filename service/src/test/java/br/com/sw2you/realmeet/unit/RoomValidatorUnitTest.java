package br.com.sw2you.realmeet.unit;

import static br.com.sw2you.realmeet.utils.TestDataCreator.newCreateRoomDTO;
import static br.com.sw2you.realmeet.validator.ValidatorConstants.*;
import static org.junit.jupiter.api.Assertions.*;

import br.com.sw2you.realmeet.core.BaseUnitTest;
import br.com.sw2you.realmeet.exception.InvalidRequestException;
import br.com.sw2you.realmeet.validator.RoomValidator;
import br.com.sw2you.realmeet.validator.ValidationError;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoomValidatorUnitTest extends BaseUnitTest {
    private RoomValidator victim;

    @BeforeEach
    void setupEach() {
        victim = new RoomValidator();
    }

    @Test
    void testValidateWhenRoomIsValid() {
        victim.validate(newCreateRoomDTO());
    }

    @Test
    void testValidateWhenRoomNameIsMissing() {
       var exception = assertThrows(InvalidRequestException.class, () -> victim.validate(newCreateRoomDTO().name(null)));

       assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
       assertEquals(new ValidationError(ROOM_NAME, ROOM_NAME + MISSING), exception.getValidationErrors().getError(0));
    }

    @Test
    void testValidateWhenRoomNameExceedsLength() {
        var exception = assertThrows(
                InvalidRequestException.class,
                () -> victim.validate(newCreateRoomDTO().name(StringUtils.rightPad("X", ROOM_NAME_MAX_LENGTH + 1, 'x')))
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(new ValidationError(ROOM_NAME, ROOM_NAME + EXCEEDS_MAX_LENGTH), exception.getValidationErrors().getError(0));
    }

    @Test
    void testValidateWhenRoomSeatsIsMissing() {
        var exception = assertThrows(InvalidRequestException.class, () -> victim.validate(newCreateRoomDTO().seats(null)));

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(new ValidationError(ROOM_SEATS, ROOM_SEATS + MISSING), exception.getValidationErrors().getError(0));
    }

    @Test
    void testValidateWhenRoomSeatsAreLessThenMinValue() {
        var exception = assertThrows(InvalidRequestException.class, () -> victim.validate(newCreateRoomDTO().seats(ROOM_SEATS_MIN_VALUE - 1)));

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(new ValidationError(ROOM_SEATS, ROOM_SEATS + BELOW_MIN_VALUE), exception.getValidationErrors().getError(0));
    }

    @Test
    void testValidateWhenRoomSeatsAreGreaterThanMaxValue() {
        var exception = assertThrows(InvalidRequestException.class, () -> victim.validate(newCreateRoomDTO().seats(ROOM_SEATS_MAX_VALUE + 1)));

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(new ValidationError(ROOM_SEATS, ROOM_SEATS + EXCEEDS_MAX_VALUE), exception.getValidationErrors().getError(0));
    }
}
