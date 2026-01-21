package com.test.assignment_2.util;

public interface MailService {
    void sendOrderConfirmation(String toEmail, String subject, String body);
}
