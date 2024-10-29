package com.ebiz.wsb.domain.sse.exception;

public class AlertNotFoundException extends RuntimeException{
    public AlertNotFoundException(String message) {
        super(message);
    }
}
