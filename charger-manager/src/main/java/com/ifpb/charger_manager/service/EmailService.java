package com.ifpb.charger_manager.service;

import com.ifpb.charger_manager.domain.model.Charge;
import com.ifpb.charger_manager.domain.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Serviço para envio de e-mails de notificação
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final CustomerService customerService;

    @Value("${spring.notifications.email.from}")
    private String fromEmail;

    @Value("${spring.notifications.email.subject-prefix}")
    private String subjectPrefix;

    public EmailService(JavaMailSender mailSender, CustomerService customerService) {
        this.mailSender = mailSender;
        this.customerService = customerService;
    }

    /**
     * Envia e-mail de notificação de alteração de status de cobrança
     */
    @Async
    public void sendChargeStatusUpdateEmail(Charge charge) {
        try {
            Customer customer = customerService.getCustomerById(charge.getCustomerId());
            
            log.info("Sending email notification to {} for charge {}", customer.getEmail(), charge.getId());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customer.getEmail());
            message.setSubject(subjectPrefix + "Atualização de Cobrança - " + charge.getStatus());
            
            String text = String.format(
                "Olá, %s!\n\n" +
                "Houve uma atualização na sua cobrança referente a: %s\n" +
                "Status Atual: %s\n" +
                "Valor: R$ %s\n" +
                "Vencimento: %s\n\n" +
                "Você pode acessar sua fatura aqui: %s\n\n" +
                "Atenciosamente,\nEquipe Charger Manager",
                customer.getName(),
                charge.getDescription(),
                charge.getStatus(),
                charge.getValue(),
                charge.getDueDate(),
                charge.getInvoiceUrl() != null ? charge.getInvoiceUrl() : "N/A"
            );

            message.setText(text);
            mailSender.send(message);
            
            log.info("Email sent successfully to {}", customer.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email for charge {}: {}", charge.getId(), e.getMessage());
        }
    }
}
