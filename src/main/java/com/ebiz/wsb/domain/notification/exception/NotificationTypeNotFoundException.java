package com.ebiz.wsb.domain.notification.exception;

public class NotificationTypeNotFoundException extends RuntimeException{

    public NotificationTypeNotFoundException(String message){
        super(message);
    }
}
