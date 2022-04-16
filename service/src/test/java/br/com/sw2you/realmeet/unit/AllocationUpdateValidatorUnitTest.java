package br.com.sw2you.realmeet.unit;

import static br.com.sw2you.realmeet.util.DateUtils.now;
import static br.com.sw2you.realmeet.utils.TestDataCreator.newUpdateAllocationDTO;
import static br.com.sw2you.realmeet.utils.TestsConstants.DEFAULT_ALLOCATION_ID;
import static br.com.sw2you.realmeet.utils.TestsConstants.DEFAULT_ROOM_ID;
import static br.com.sw2you.realmeet.validator.ValidatorConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.sw2you.realmeet.core.BaseUnitTest;
import br.com.sw2you.realmeet.domain.repository.AllocationRepository;
import br.com.sw2you.realmeet.exception.InvalidRequestException;
import br.com.sw2you.realmeet.validator.AllocationValidator;
import br.com.sw2you.realmeet.validator.ValidationError;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class AllocationUpdateValidatorUnitTest extends BaseUnitTest {
    private AllocationValidator victim;

    @Mock
    AllocationRepository allocationRepository;

    @BeforeEach
    void setupEach() {
        victim = new AllocationValidator(allocationRepository);
    }

    @Test
    void testValidateWhenAllocationIsValid() {
        victim.validate(DEFAULT_ALLOCATION_ID, DEFAULT_ROOM_ID, newUpdateAllocationDTO());
    }

    @Test
    void testValidateWhenAllocationIdIsMissing() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () -> victim.validate(null, DEFAULT_ROOM_ID, newUpdateAllocationDTO())
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_ID, ALLOCATION_ID + MISSING),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenSubjectIsMissing() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () -> victim.validate(DEFAULT_ALLOCATION_ID, DEFAULT_ROOM_ID, newUpdateAllocationDTO().subject(null))
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_SUBJECT, ALLOCATION_SUBJECT + MISSING),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenSubjectExceedsLength() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () ->
                victim.validate(
                    DEFAULT_ALLOCATION_ID,
                    DEFAULT_ROOM_ID,
                    newUpdateAllocationDTO().subject(StringUtils.rightPad("X", ALLOCATION_SUBJECT_MAX_LENGTH + 1, 'x'))
                )
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_SUBJECT, ALLOCATION_SUBJECT + EXCEEDS_MAX_LENGTH),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenStartAtIsMissing() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () -> victim.validate(DEFAULT_ALLOCATION_ID, DEFAULT_ROOM_ID, newUpdateAllocationDTO().startAt(null))
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_START_AT, ALLOCATION_START_AT + MISSING),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenEndAtIsMissing() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () -> victim.validate(DEFAULT_ALLOCATION_ID, DEFAULT_ROOM_ID, newUpdateAllocationDTO().endAt(null))
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_END_AT, ALLOCATION_END_AT + MISSING),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenDateOrderIsInvalid() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () ->
                victim.validate(
                    DEFAULT_ALLOCATION_ID,
                    DEFAULT_ROOM_ID,
                    newUpdateAllocationDTO().startAt(now().plusDays(1)).endAt(now().plusDays(1).minusMinutes(30))
                )
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_START_AT, ALLOCATION_START_AT + INCONSISTENT),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenDateIntervalExceedsMaxDuration() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () ->
                victim.validate(
                    DEFAULT_ALLOCATION_ID,
                    DEFAULT_ROOM_ID,
                    newUpdateAllocationDTO()
                        .startAt(now().plusDays(1))
                        .endAt(now().plusDays(1).plusSeconds(ALLOCATION_MAX_DURATION_SECONDS + 1))
                )
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_END_AT, ALLOCATION_END_AT + EXCEEDS_DURATION),
            exception.getValidationErrors().getError(0)
        );
    }
}
