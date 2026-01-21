package com.test.assignment_2.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnMissingBean(MailService.class)
public class NoopMailService implements MailService {
  @Override
  public void sendOrderConfirmation(String toEmail, String subject, String body) {
    log.info("[MAIL][NOOP] to={}, subject={}, body={}", toEmail, subject, body);
  }
}
