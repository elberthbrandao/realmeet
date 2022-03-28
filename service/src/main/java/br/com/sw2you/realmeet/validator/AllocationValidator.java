package br.com.sw2you.realmeet.validator;

import static br.com.sw2you.realmeet.validator.ValidatorConstants.*;
import static br.com.sw2you.realmeet.validator.ValidatorUtils.*;

import br.com.sw2you.realmeet.api.model.CreateAllocationDTO;
import br.com.sw2you.realmeet.api.model.CreateRoomDTO;
import br.com.sw2you.realmeet.api.model.UpdateRoomDTO;
import br.com.sw2you.realmeet.domain.repository.AllocationRepository;
import br.com.sw2you.realmeet.domain.repository.RoomRepository;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class AllocationValidator {
    private final AllocationRepository allocationRepository;

    public AllocationValidator(AllocationRepository allocationRepository) {
        this.allocationRepository = allocationRepository;
    }

    public void validate(CreateAllocationDTO createAllocationDTO) {
        var validationErrors = new ValidationErrors();
        validateSubject(createAllocationDTO.getSubject(), validationErrors);
        validateEmployeeName(createAllocationDTO.getEmployeeName(), validationErrors);
        validateEmployeeEmail(createAllocationDTO.getEmployeeEmail(), validationErrors);
        throwOnError(validationErrors);
    }

    private boolean validateSubject(String subject, ValidationErrors validationErrors) {
        return (
            validateRequired(subject, ALLOCATION_SUBJECT, validationErrors) &&
            validateMaxLength(subject, ALLOCATION_SUBJECT, ALLOCATION_SUBJECT_MAX_LENGTH, validationErrors)
        );
    }

    private boolean validateEmployeeName(String employeeName, ValidationErrors validationErrors) {
        return (
            validateRequired(employeeName, ALLOCATION_EMPLOYEE_NAME, validationErrors) &&
            validateMaxLength(employeeName, ALLOCATION_EMPLOYEE_NAME, ALLOCATION_EMPLOYEE_NAME_MAX_LENGTH, validationErrors)
        );
    }

    private boolean validateEmployeeEmail(String employeeEmail, ValidationErrors validationErrors) {
        return (
            validateRequired(employeeEmail, ALLOCATION_EMPLOYEE_EMAIL, validationErrors) &&
            validateMaxLength(employeeEmail, ALLOCATION_EMPLOYEE_EMAIL, ALLOCATION_EMPLOYEE_EMAIL_MAX_LENGTH, validationErrors)
        );
    }
}
