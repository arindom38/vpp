package com.challenge.vpp.exception;

public class InvalidPostcodeRangeException extends RuntimeException {
    public InvalidPostcodeRangeException(String message) {
        super(message);
    }
}