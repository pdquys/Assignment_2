package com.test.assignment_2.util;


import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Component;

@Component("springMailService")
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
@RequiredArgsConstructor
@Primary
public class SpringMailService implements MailService {

  private final JavaMailSender mailSender;

  @Override
  public void sendOrderConfirmation(String toEmail, String subject, String body) {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
    try {
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(body, false);
      mailSender.send(message);
    } catch (Exception e) {
      // swallow in phase 1; production should retry / outbox
    }
  }
}
