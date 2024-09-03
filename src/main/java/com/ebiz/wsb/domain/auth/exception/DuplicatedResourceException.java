package com.ebiz.wsb.domain.auth.exception;

public class DuplicatedResourceException extends RuntimeException{
    public DuplicatedResourceException(String message){
        super(message);
    }
}
