package com.test.assignment_2.exception;
public class BadRequestException extends BusinessException {
  public BadRequestException(String code, String message) {
    super(code, message);
  }
}
