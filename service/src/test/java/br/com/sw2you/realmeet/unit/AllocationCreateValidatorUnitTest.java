package br.com.sw2you.realmeet.unit;

import static br.com.sw2you.realmeet.util.DateUtils.now;
import static br.com.sw2you.realmeet.utils.TestDataCreator.*;
import static br.com.sw2you.realmeet.utils.TestsConstants.*;
import static br.com.sw2you.realmeet.validator.ValidatorConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.sw2you.realmeet.core.BaseUnitTest;
import br.com.sw2you.realmeet.domain.entity.Allocation;
import br.com.sw2you.realmeet.domain.repository.AllocationRepository;
import br.com.sw2you.realmeet.exception.InvalidRequestException;
import br.com.sw2you.realmeet.utils.TestsConstants;
import br.com.sw2you.realmeet.validator.AllocationValidator;
import br.com.sw2you.realmeet.validator.ValidationError;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class AllocationCreateValidatorUnitTest extends BaseUnitTest {
    private AllocationValidator victim;

    @Mock
    AllocationRepository allocationRepository;

    @BeforeEach
    void setupEach() {
        victim = new AllocationValidator(allocationRepository);
    }

    @Test
    void testValidateWhenAllocationIsValid() {
        victim.validate(newCreateAllocationDTO());
    }

    @Test
    void testValidateWhenSubjectIsMissing() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () -> victim.validate(newCreateAllocationDTO().subject(null))
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
                    newCreateAllocationDTO().subject(StringUtils.rightPad("X", ALLOCATION_SUBJECT_MAX_LENGTH + 1, 'x'))
                )
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_SUBJECT, ALLOCATION_SUBJECT + EXCEEDS_MAX_LENGTH),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenEmployeeNameIsMissing() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () -> victim.validate(newCreateAllocationDTO().employeeName(null))
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_EMPLOYEE_NAME, ALLOCATION_EMPLOYEE_NAME + MISSING),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenEmployeeNameExceedsLength() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () ->
                victim.validate(
                    newCreateAllocationDTO()
                        .employeeName(StringUtils.rightPad("X", ALLOCATION_EMPLOYEE_NAME_MAX_LENGTH + 1, 'x'))
                )
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_EMPLOYEE_NAME, ALLOCATION_EMPLOYEE_NAME + EXCEEDS_MAX_LENGTH),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenEmployeeEmailIsMissing() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () -> victim.validate(newCreateAllocationDTO().employeeEmail(null))
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_EMPLOYEE_EMAIL, ALLOCATION_EMPLOYEE_EMAIL + MISSING),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenEmployeeEmailExceedsLength() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () ->
                victim.validate(
                    newCreateAllocationDTO()
                        .employeeEmail(StringUtils.rightPad("X", ALLOCATION_EMPLOYEE_EMAIL_MAX_LENGTH + 1, 'x'))
                )
        );

        assertEquals(exception.getValidationErrors().getNumberOfErrors(), 1);
        assertEquals(
            new ValidationError(ALLOCATION_EMPLOYEE_EMAIL, ALLOCATION_EMPLOYEE_EMAIL + EXCEEDS_MAX_LENGTH),
            exception.getValidationErrors().getError(0)
        );
    }

    @Test
    void testValidateWhenStartAtIsMissing() {
        var exception = assertThrows(
            InvalidRequestException.class,
            () -> victim.validate(newCreateAllocationDTO().startAt(null))
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
            () -> victim.validate(newCreateAllocationDTO().endAt(null))
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
                    newCreateAllocationDTO().startAt(now().plusDays(1)).endAt(now().plusDays(1).minusMinutes(30))
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
                    newCreateAllocationDTO()
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

    private List<Allocation> persistAllocations(int numberOfAllocations) {
        var room = newRoomBuilder().build();

        return IntStream
            .range(0, 10)
            .mapToObj(
                i ->
                    allocationRepository.saveAndFlush(
                        newAllocationBuilder(room)
                            .subject(DEFAULT_ALLOCATION_SUBJECT + "_" + (i + 1))
                            .startAt(DEFAULT_ALLOCATION_START_AT.plusHours(i + 1))
                            .endAt(DEFAULT_ALLOCATION_END_AT.plusHours(i + 1))
                            .build())
            )
            .collect(Collectors.toList());
    }
}
