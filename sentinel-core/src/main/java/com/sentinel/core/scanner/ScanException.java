package com.sentinel.core.scanner;

public class ScanException extends Exception {
    public ScanException(String message) {
        super(message);
    }
    
    public ScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
