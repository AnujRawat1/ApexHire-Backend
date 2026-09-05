package com.ApexHire.resume.exception;

public class ResumeFileException extends RuntimeException {
    public ResumeFileException(String message) {
        super(message);
    }

    public ResumeFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
