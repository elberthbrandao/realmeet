package br.com.sw2you.realmeet.service;

import br.com.sw2you.realmeet.domain.entity.Allocation;
import br.com.sw2you.realmeet.email.EmailInfoBuilder;
import br.com.sw2you.realmeet.email.EmailSender;
import br.com.sw2you.realmeet.email.TemplateType;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final String ALLOCATION = "allocation";
    private final EmailSender emailSender;
    private final EmailInfoBuilder emailInfoBuilder;

    public NotificationService(
        EmailSender emailSender,
        EmailInfoBuilder emailInfoBuilder
    ) {
        this.emailSender = emailSender;
        this.emailInfoBuilder = emailInfoBuilder;
    }

    public void notifyAllocationCreated(Allocation allocation) {
        notify(allocation, TemplateType.ALLOCATION_CREATED);
    }

    public void notifyAllocationUpdated(Allocation allocation) {
        notify(allocation, TemplateType.ALLOCATION_UPDATED);
    }

    public void notifyAllocationDeleted(Allocation allocation) {
        notify(allocation, TemplateType.ALLOCATION_DELETED);
    }

    private void notify(Allocation allocation, TemplateType templateType) {
        emailSender.send(
            emailInfoBuilder.createEmailInfo(
                allocation.getEmployee().getEmail(),
                templateType,
                Map.of(ALLOCATION, allocation)
        ));
    }
}
