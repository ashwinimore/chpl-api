package gov.healthit.chpl.changerequest.domain.service.email;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;

@Component
public class AttestationAcceptedEmail extends ChangeRequestEmail {
        private ChplEmailFactory chplEmailFactory;
        private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
        private String emailSubject;
        private String emailBody;


        @Autowired
        public AttestationAcceptedEmail(ChplEmailFactory chplEmailFactory, ChplHtmlEmailBuilder chplHtmlEmailBuilder, UserDeveloperMapDAO userDeveloperMapDAO,
                @Value("${changeRequest.attestation.approval.subject}") String emailSubject,
                @Value("${changeRequest.attestation.approval.body}") String emailBody) {

            super(userDeveloperMapDAO);
            this.chplEmailFactory = chplEmailFactory;
            this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
            this.emailSubject = emailSubject;
            this.emailBody = emailBody;
        }


        @Override
        public void send(ChangeRequest cr) throws EmailNotSentException {
            chplEmailFactory.emailBuilder()
                .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(emailSubject)
                .htmlMessage(createAcceptedHtmlMessage(cr))
                .sendEmail();

        }

        private String createAcceptedHtmlMessage(ChangeRequest cr) {
            return chplHtmlEmailBuilder.initialize()
                    .heading("Developer Attestations Accepted")
                    .paragraph("", String.format(emailBody,
                            cr.getSubmittedDateTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                            getApprovalBody(cr)))
                    .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                    .footer(true)
                    .build();
        }

}
