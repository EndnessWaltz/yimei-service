package com.ymcoffee.base.exception;

public class ServiceException extends GlobalException {
    public ServiceException(ExceptionCode code, String message) {
        super(code, message);
    }
}
