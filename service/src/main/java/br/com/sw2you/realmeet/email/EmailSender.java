package br.com.sw2you.realmeet.email;

import static br.com.sw2you.realmeet.util.StringUtils.join;
import static java.util.Objects.nonNull;

import br.com.sw2you.realmeet.email.model.Attachment;
import br.com.sw2you.realmeet.email.model.EmailInfo;
import br.com.sw2you.realmeet.exception.EmailSendingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);
    private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=UTF-8";

    private final JavaMailSender javaMailSender;
    private final ITemplateEngine templateEngine;

    public EmailSender(JavaMailSender javaMailSender, ITemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void send(EmailInfo emailInfo) {
        LOGGER.info("Sending e-mail with subject '{}' to '{}'", emailInfo.getSubject(), emailInfo.getTo());

        var mimeMessage = javaMailSender.createMimeMessage();
        var multiPart = new MimeMultipart();

        addBasicDatails(emailInfo, mimeMessage);
        addHtmlBody(emailInfo.getTemplate(), emailInfo.getTemplateData(), multiPart);
        addAttachments(emailInfo.getAttachments(), multiPart);
        setContent(mimeMessage, multiPart);

        javaMailSender.send(mimeMessage);
    }

    private void addBasicDatails(EmailInfo emailInfo, MimeMessage mimeMessage) {
        try {
            mimeMessage.setFrom(emailInfo.getFrom());
            mimeMessage.setSubject(emailInfo.getSubject());
            mimeMessage.addRecipients(Message.RecipientType.TO, join(emailInfo.getTo()));

            if(nonNull(emailInfo.getCc())) {
                mimeMessage.addRecipients(Message.RecipientType.TO, join(emailInfo.getCc()));
            }

            if(nonNull(emailInfo.getBcc())) {
                mimeMessage.addRecipients(Message.RecipientType.TO, join(emailInfo.getBcc()));
            }
        } catch (MessagingException e) {
            throwEmailSendingException(e, "Error adding data to MIME Message");
        }
    }

    private void addHtmlBody(String template, Map<String, Object> templateData, MimeMultipart multiPart) {
        try {
            var messageHtmlPart = new MimeBodyPart();
            var context = new Context();

            if (nonNull(templateData)) {
                context.setVariables(templateData);
            }

            messageHtmlPart.setContent(templateEngine.process(template, context), TEXT_HTML_CHARSET_UTF_8);
            multiPart.addBodyPart(messageHtmlPart);
        } catch (MessagingException e) {
            throwEmailSendingException(e, "Error adding HTML content to MIME Message");
        }
    }

    private void addAttachments(List<Attachment> attachments, MimeMultipart multiPart) {
        if(nonNull(attachments)) {
            attachments.forEach(
                a -> {
                    try {
                        var messageAttachmentPart = new MimeBodyPart();
                        messageAttachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(a.getImputStream(), a.getContentType())));
                        messageAttachmentPart.setFileName(a.getFileName());
                        multiPart.addBodyPart(messageAttachmentPart);
                    } catch (MessagingException | IOException e) {
                        throwEmailSendingException(e, "Error adding attachments to MIME Message");
                    }
                }
            );
        }
    }

    private void setContent(MimeMessage mimeMessage, MimeMultipart multiPart) {
        try {
            mimeMessage.setContent(multiPart);
        } catch (MessagingException e) {
            throwEmailSendingException(e, "Error setting content to MIME Message");
        }
    }

    private void throwEmailSendingException(Exception exception, String errorMessage) {
        var fullErrorMessage = String.format("%s : %s", exception.getMessage(), errorMessage);
        LOGGER.error(fullErrorMessage);
        throw new EmailSendingException(fullErrorMessage, exception);
    }
}
